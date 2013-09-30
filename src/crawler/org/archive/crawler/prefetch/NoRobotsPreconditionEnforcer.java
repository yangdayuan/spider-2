package org.archive.crawler.prefetch;

import java.util.Iterator;
import java.util.Set;

import javax.management.AttributeNotFoundException;

import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.archive.crawler.datamodel.CoreAttributeConstants;
import org.archive.crawler.datamodel.CrawlHost;
import org.archive.crawler.datamodel.CrawlServer;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.datamodel.CredentialStore;
import org.archive.crawler.datamodel.FetchStatusCodes;
import org.archive.crawler.datamodel.credential.Credential;
import org.archive.crawler.datamodel.credential.CredentialAvatar;
import org.archive.crawler.framework.Processor;
import org.archive.crawler.settings.SimpleType;
import org.archive.crawler.settings.Type;

public class NoRobotsPreconditionEnforcer extends Processor
        implements CoreAttributeConstants, FetchStatusCodes {

    private static final long serialVersionUID = 4637898153589079615L;

    private static final Logger logger = Logger.getLogger(NoRobotsPreconditionEnforcer.class.getName());

    private final static Integer DEFAULT_IP_VALIDITY_DURATION = 
        new Integer(60*60*6); // six hours 

    /** seconds to keep IP information for */
    public final static String ATTR_IP_VALIDITY_DURATION
        = "ip-validity-duration-seconds";
    
    public NoRobotsPreconditionEnforcer(String name) {
        super(name, "No Robots Precondition enforcer");

        Type e;

        e = addElementToDefinition(new SimpleType(ATTR_IP_VALIDITY_DURATION,
                "The minimum interval for which a dns-record will be considered " +
                "valid (in seconds). " +
                "If the record's DNS TTL is larger, that will be used instead.",
                DEFAULT_IP_VALIDITY_DURATION));
        e.setExpertSetting(true);
    }

    protected void innerProcess(CrawlURI curi) {

        if (considerDnsPreconditions(curi)) {
            return;
        }

        // make sure we only process schemes we understand (i.e. not dns)
        String scheme = curi.getUURI().getScheme().toLowerCase();
        if (! (scheme.equals("http") || scheme.equals("https"))) {
            logger.debug("PolitenessEnforcer doesn't understand uri's of type " +
                scheme + " (ignoring)");
            return;
        }

        if (!curi.isPrerequisite() && credentialPrecondition(curi)) {
            return;
        }

        // OK, it's allowed

        // For all curis that will in fact be fetched, set appropriate delays.
        // TODO: SOMEDAY: allow per-host, per-protocol, etc. factors
        // curi.setDelayFactor(getDelayFactorFor(curi));
        // curi.setMinimumDelay(getMinimumDelayFor(curi));

        return;
    }

    /**
     * @param curi CrawlURI whose dns prerequisite we're to check.
     * @return true if no further processing in this module should occur
     */
    private boolean considerDnsPreconditions(CrawlURI curi) {
        if(curi.getUURI().getScheme().equals("dns")){
            // DNS URIs never have a DNS precondition
            curi.setPrerequisite(true);
            return false; 
        }
        
        CrawlServer cs = getController().getServerCache().getServerFor(curi);
        if(cs == null) {
            curi.setFetchStatus(S_UNFETCHABLE_URI);
            curi.skipToProcessorChain(getController().getPostprocessorChain());
            return true;
        }

        // If we've done a dns lookup and it didn't resolve a host
        // cancel further fetch-processing of this URI, because
        // the domain is unresolvable
        CrawlHost ch = getController().getServerCache().getHostFor(curi);
        if (ch == null || ch.hasBeenLookedUp() && ch.getIP() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("no dns for " + ch +
                    " cancelling processing for CrawlURI " + curi.toString());
            }
            curi.setFetchStatus(S_DOMAIN_PREREQUISITE_FAILURE);
            curi.skipToProcessorChain(getController().getPostprocessorChain());
            return true;
        }

        // If we haven't done a dns lookup  and this isn't a dns uri
        // shoot that off and defer further processing
        if (isIpExpired(curi) && !curi.getUURI().getScheme().equals("dns")) {
            logger.debug("Deferring processing of CrawlURI " + curi.toString()
                + " for dns lookup.");
            String preq = "dns:" + ch.getHostName();
            try {
                curi.markPrerequisite(preq,
                    getController().getPostprocessorChain());
            } catch (URIException e) {
                throw new RuntimeException(e); // shouldn't ever happen
            }
            return true;
        }
        
        // DNS preconditions OK
        return false;
    }

    /**
     * Get the maximum time a dns-record is valid.
     *
     * @param curi the uri this time is valid for.
     * @return the maximum time a dns-record is valid -- in seconds -- or
     * negative if record's ttl should be used.
     */
    public long getIPValidityDuration(CrawlURI curi) {
        Integer d;
        try {
            d = (Integer)getAttribute(ATTR_IP_VALIDITY_DURATION, curi);
        } catch (AttributeNotFoundException e) {
            d = DEFAULT_IP_VALIDITY_DURATION;
        }

        return d.longValue();
    }

    /** Return true if ip should be looked up.
     *
     * @param curi the URI to check.
     * @return true if ip should be looked up.
     */
    public boolean isIpExpired(CrawlURI curi) {
        CrawlHost host = getController().getServerCache().getHostFor(curi);
        if (!host.hasBeenLookedUp()) {
            // IP has not been looked up yet.
            return true;
        }

        if (host.getIpTTL() == CrawlHost.IP_NEVER_EXPIRES) {
            // IP never expires (numeric IP)
            return false;
        }

        long duration = getIPValidityDuration(curi);
        if (duration == 0) {
            // Never expire ip if duration is null (set by user or more likely,
            // set to zero in case where we tried in FetchDNS but failed).
            return false;
        }

        // catch old "default" -1 settings that are now problematic,
        // convert to new minimum
        if (duration <= 0) {
            duration = DEFAULT_IP_VALIDITY_DURATION.intValue();
        }
        
        long ttl = host.getIpTTL();
        if (ttl > duration) {
            // Use the larger of the operator-set minimum duration 
            // or the DNS record TTL
            duration = ttl;
        }

        // Duration and ttl are in seconds.  Convert to millis.
        if (duration > 0) {
            duration *= 1000;
        }

        return (duration + host.getIpFetched()) < System.currentTimeMillis();
    }

   /**
    * Consider credential preconditions.
    *
    * Looks to see if any credential preconditions (e.g. html form login
    * credentials) for this <code>CrawlServer</code>. If there are, have they
    * been run already? If not, make the running of these logins a precondition
    * of accessing any other url on this <code>CrawlServer</code>.
    *
    * <p>
    * One day, do optimization and avoid running the bulk of the code below.
    * Argument for running the code everytime is that overrides and refinements
    * may change what comes back from credential store.
    *
    * @param curi CrawlURI we're checking for any required preconditions.
    * @return True, if this <code>curi</code> has a precondition that needs to
    *         be met before we can proceed. False if we can precede to process
    *         this url.
    */
    @SuppressWarnings("unchecked")
	private boolean credentialPrecondition(final CrawlURI curi) {

        boolean result = false;

        CredentialStore cs =
            CredentialStore.getCredentialStore(getSettingsHandler());
        if (cs == null) {
            logger.error("No credential store for " + curi);
            return result;
        }

        Iterator i = cs.iterator(curi);
        if (i == null) {
            return result;
        }

        while (i.hasNext()) {
            Credential c = (Credential)i.next();

            if (c.isPrerequisite(curi)) {
                // This credential has a prereq. and this curi is it.  Let it
                // through.  Add its avatar to the curi as a mark.  Also, does
                // this curi need to be posted?  Note, we do this test for
                // is it a prereq BEFORE we do the check that curi is of the
                // credential domain because such as yahoo have you go to
                // another domain altogether to login.
                c.attach(curi);
                curi.setPost(c.isPost(curi));
                break;
            }

            if (!c.rootUriMatch(getController(), curi)) {
                continue;
            }

            if (!c.hasPrerequisite(curi)) {
                continue;
            }

            if (!authenticated(c, curi)) {
                // Han't been authenticated.  Queue it and move on (Assumption
                // is that we can do one authentication at a time -- usually one
                // html form).
                String prereq = c.getPrerequisite(curi);
                if (prereq == null || prereq.length() <= 0) {
                    CrawlServer server =
                        getController().getServerCache().getServerFor(curi);
                    logger.error(server.getName() + " has "
                        + " credential(s) of type " + c + " but prereq"
                        + " is null.");
                } else {
                    try {
                        curi.markPrerequisite(prereq,
                            getController().getPostprocessorChain());
                    } catch (URIException e) {
                        logger.error("unable to set credentials prerequisite "+prereq);
                        getController().logUriError(e,curi.getUURI(),prereq);
                        return false; 
                    }
                    result = true;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Queueing prereq " + prereq + " of type " +
                            c + " for " + curi);
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Has passed credential already been authenticated.
     *
     * @param credential Credential to test.
     * @param curi CrawlURI.
     * @return True if already run.
     */
    @SuppressWarnings("unchecked")
	private boolean authenticated(final Credential credential,
            final CrawlURI curi) {
        boolean result = false;
        CrawlServer server =
            getController().getServerCache().getServerFor(curi);
        if (!server.hasCredentialAvatars()) {
            return result;
        }
        Set avatars = server.getCredentialAvatars();
        for (Iterator i = avatars.iterator(); i.hasNext();) {
            CredentialAvatar ca = (CredentialAvatar)i.next();
            String key = null;
            try {
                key = credential.getKey(curi);
            } catch (AttributeNotFoundException e) {
                logger.error("Failed getting key for " + credential +
                    " for " + curi);
                continue;
            }
            if (ca.match(credential.getClass(), key)) {
                result = true;
            }
        }
        return result;
    }

}
