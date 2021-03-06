/**
 * Autogenerated by Thrift Compiler (0.7.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.netease.backend.collector.rss.common.net;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseService {

  /**
   * 所有service的基类，定义一些通用接口
   */
  public interface Iface {

    public void ping(String server, int port) throws DTException, org.apache.thrift.TException;

  }

  public interface AsyncIface {

    public void ping(String server, int port, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.ping_call> resultHandler) throws org.apache.thrift.TException;

  }

  public static class Client extends org.apache.thrift.TServiceClient implements Iface {
    public static class Factory implements org.apache.thrift.TServiceClientFactory<Client> {
      public Factory() {}
      public Client getClient(org.apache.thrift.protocol.TProtocol prot) {
        return new Client(prot);
      }
      public Client getClient(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
        return new Client(iprot, oprot);
      }
    }

    public Client(org.apache.thrift.protocol.TProtocol prot)
    {
      super(prot, prot);
    }

    public Client(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
      super(iprot, oprot);
    }

    public void ping(String server, int port) throws DTException, org.apache.thrift.TException
    {
      send_ping(server, port);
      recv_ping();
    }

    public void send_ping(String server, int port) throws org.apache.thrift.TException
    {
      ping_args args = new ping_args();
      args.setServer(server);
      args.setPort(port);
      sendBase("ping", args);
    }

    public void recv_ping() throws DTException, org.apache.thrift.TException
    {
      ping_result result = new ping_result();
      receiveBase(result, "ping");
      if (result.e != null) {
        throw result.e;
      }
      return;
    }

  }
  public static class AsyncClient extends org.apache.thrift.async.TAsyncClient implements AsyncIface {
    public static class Factory implements org.apache.thrift.async.TAsyncClientFactory<AsyncClient> {
      private org.apache.thrift.async.TAsyncClientManager clientManager;
      private org.apache.thrift.protocol.TProtocolFactory protocolFactory;
      public Factory(org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.protocol.TProtocolFactory protocolFactory) {
        this.clientManager = clientManager;
        this.protocolFactory = protocolFactory;
      }
      public AsyncClient getAsyncClient(org.apache.thrift.transport.TNonblockingTransport transport) {
        return new AsyncClient(protocolFactory, clientManager, transport);
      }
    }

    public AsyncClient(org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.transport.TNonblockingTransport transport) {
      super(protocolFactory, clientManager, transport);
    }

    public void ping(String server, int port, org.apache.thrift.async.AsyncMethodCallback<ping_call> resultHandler) throws org.apache.thrift.TException {
      checkReady();
      ping_call method_call = new ping_call(server, port, resultHandler, this, ___protocolFactory, ___transport);
      this.___currentMethod = method_call;
      ___manager.call(method_call);
    }

    public static class ping_call extends org.apache.thrift.async.TAsyncMethodCall {
      private String server;
      private int port;
      public ping_call(String server, int port, org.apache.thrift.async.AsyncMethodCallback<ping_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.server = server;
        this.port = port;
      }

      public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
        prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("ping", org.apache.thrift.protocol.TMessageType.CALL, 0));
        ping_args args = new ping_args();
        args.setServer(server);
        args.setPort(port);
        args.write(prot);
        prot.writeMessageEnd();
      }

      public void getResult() throws DTException, org.apache.thrift.TException {
        if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_ping();
      }
    }

  }

  public static class Processor<I extends Iface> extends org.apache.thrift.TBaseProcessor implements org.apache.thrift.TProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class.getName());
    public Processor(I iface) {
      super(iface, getProcessMap(new HashMap<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>>()));
    }

    protected Processor(I iface, Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> processMap) {
      super(iface, getProcessMap(processMap));
    }

    private static <I extends Iface> Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> getProcessMap(Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> processMap) {
      processMap.put("ping", new ping());
      return processMap;
    }

    private static class ping<I extends Iface> extends org.apache.thrift.ProcessFunction<I, ping_args> {
      public ping() {
        super("ping");
      }

      protected ping_args getEmptyArgsInstance() {
        return new ping_args();
      }

      protected ping_result getResult(I iface, ping_args args) throws org.apache.thrift.TException {
        ping_result result = new ping_result();
        try {
          iface.ping(args.server, args.port);
        } catch (DTException e) {
          result.e = e;
        }
        return result;
      }
    }

  }

  public static class ping_args implements org.apache.thrift.TBase<ping_args, ping_args._Fields>, java.io.Serializable, Cloneable   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ping_args");

    private static final org.apache.thrift.protocol.TField SERVER_FIELD_DESC = new org.apache.thrift.protocol.TField("server", org.apache.thrift.protocol.TType.STRING, (short)1);
    private static final org.apache.thrift.protocol.TField PORT_FIELD_DESC = new org.apache.thrift.protocol.TField("port", org.apache.thrift.protocol.TType.I32, (short)2);

    public String server; // required
    public int port; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      SERVER((short)1, "server"),
      PORT((short)2, "port");

      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, or null if its not found.
       */
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: // SERVER
            return SERVER;
          case 2: // PORT
            return PORT;
          default:
            return null;
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, throwing an exception
       * if it is not found.
       */
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }

      /**
       * Find the _Fields constant that matches name, or null if its not found.
       */
      public static _Fields findByName(String name) {
        return byName.get(name);
      }

      private final short _thriftId;
      private final String _fieldName;

      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }

      public short getThriftFieldId() {
        return _thriftId;
      }

      public String getFieldName() {
        return _fieldName;
      }
    }

    // isset id assignments
    private static final int __PORT_ISSET_ID = 0;
    private BitSet __isset_bit_vector = new BitSet(1);

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
      Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SERVER, new org.apache.thrift.meta_data.FieldMetaData("server", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
      tmpMap.put(_Fields.PORT, new org.apache.thrift.meta_data.FieldMetaData("port", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ping_args.class, metaDataMap);
    }

    public ping_args() {
    }

    public ping_args(
      String server,
      int port)
    {
      this();
      this.server = server;
      this.port = port;
      setPortIsSet(true);
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public ping_args(ping_args other) {
      __isset_bit_vector.clear();
      __isset_bit_vector.or(other.__isset_bit_vector);
      if (other.isSetServer()) {
        this.server = other.server;
      }
      this.port = other.port;
    }

    public ping_args deepCopy() {
      return new ping_args(this);
    }

    @Override
    public void clear() {
      this.server = null;
      setPortIsSet(false);
      this.port = 0;
    }

    public String getServer() {
      return this.server;
    }

    public ping_args setServer(String server) {
      this.server = server;
      return this;
    }

    public void unsetServer() {
      this.server = null;
    }

    /** Returns true if field server is set (has been assigned a value) and false otherwise */
    public boolean isSetServer() {
      return this.server != null;
    }

    public void setServerIsSet(boolean value) {
      if (!value) {
        this.server = null;
      }
    }

    public int getPort() {
      return this.port;
    }

    public ping_args setPort(int port) {
      this.port = port;
      setPortIsSet(true);
      return this;
    }

    public void unsetPort() {
      __isset_bit_vector.clear(__PORT_ISSET_ID);
    }

    /** Returns true if field port is set (has been assigned a value) and false otherwise */
    public boolean isSetPort() {
      return __isset_bit_vector.get(__PORT_ISSET_ID);
    }

    public void setPortIsSet(boolean value) {
      __isset_bit_vector.set(__PORT_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SERVER:
        if (value == null) {
          unsetServer();
        } else {
          setServer((String)value);
        }
        break;

      case PORT:
        if (value == null) {
          unsetPort();
        } else {
          setPort((Integer)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SERVER:
        return getServer();

      case PORT:
        return Integer.valueOf(getPort());

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case SERVER:
        return isSetServer();
      case PORT:
        return isSetPort();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof ping_args)
        return this.equals((ping_args)that);
      return false;
    }

    public boolean equals(ping_args that) {
      if (that == null)
        return false;

      boolean this_present_server = true && this.isSetServer();
      boolean that_present_server = true && that.isSetServer();
      if (this_present_server || that_present_server) {
        if (!(this_present_server && that_present_server))
          return false;
        if (!this.server.equals(that.server))
          return false;
      }

      boolean this_present_port = true;
      boolean that_present_port = true;
      if (this_present_port || that_present_port) {
        if (!(this_present_port && that_present_port))
          return false;
        if (this.port != that.port)
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(ping_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      ping_args typedOther = (ping_args)other;

      lastComparison = Boolean.valueOf(isSetServer()).compareTo(typedOther.isSetServer());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetServer()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.server, typedOther.server);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetPort()).compareTo(typedOther.isSetPort());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetPort()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.port, typedOther.port);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }

    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: // SERVER
            if (field.type == org.apache.thrift.protocol.TType.STRING) {
              this.server = iprot.readString();
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: // PORT
            if (field.type == org.apache.thrift.protocol.TType.I32) {
              this.port = iprot.readI32();
              setPortIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
      validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (this.server != null) {
        oprot.writeFieldBegin(SERVER_FIELD_DESC);
        oprot.writeString(this.server);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(PORT_FIELD_DESC);
      oprot.writeI32(this.port);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("ping_args(");
      boolean first = true;

      sb.append("server:");
      if (this.server == null) {
        sb.append("null");
      } else {
        sb.append(this.server);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("port:");
      sb.append(this.port);
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
      // check for required fields
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
      try {
        write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
      try {
        // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
        __isset_bit_vector = new BitSet(1);
        read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

  }

  public static class ping_result implements org.apache.thrift.TBase<ping_result, ping_result._Fields>, java.io.Serializable, Cloneable   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ping_result");

    private static final org.apache.thrift.protocol.TField E_FIELD_DESC = new org.apache.thrift.protocol.TField("e", org.apache.thrift.protocol.TType.STRUCT, (short)1);

    public DTException e; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      E((short)1, "e");

      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, or null if its not found.
       */
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: // E
            return E;
          default:
            return null;
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, throwing an exception
       * if it is not found.
       */
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }

      /**
       * Find the _Fields constant that matches name, or null if its not found.
       */
      public static _Fields findByName(String name) {
        return byName.get(name);
      }

      private final short _thriftId;
      private final String _fieldName;

      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }

      public short getThriftFieldId() {
        return _thriftId;
      }

      public String getFieldName() {
        return _fieldName;
      }
    }

    // isset id assignments

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
      Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.E, new org.apache.thrift.meta_data.FieldMetaData("e", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ping_result.class, metaDataMap);
    }

    public ping_result() {
    }

    public ping_result(
      DTException e)
    {
      this();
      this.e = e;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public ping_result(ping_result other) {
      if (other.isSetE()) {
        this.e = new DTException(other.e);
      }
    }

    public ping_result deepCopy() {
      return new ping_result(this);
    }

    @Override
    public void clear() {
      this.e = null;
    }

    public DTException getE() {
      return this.e;
    }

    public ping_result setE(DTException e) {
      this.e = e;
      return this;
    }

    public void unsetE() {
      this.e = null;
    }

    /** Returns true if field e is set (has been assigned a value) and false otherwise */
    public boolean isSetE() {
      return this.e != null;
    }

    public void setEIsSet(boolean value) {
      if (!value) {
        this.e = null;
      }
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case E:
        if (value == null) {
          unsetE();
        } else {
          setE((DTException)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case E:
        return getE();

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case E:
        return isSetE();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof ping_result)
        return this.equals((ping_result)that);
      return false;
    }

    public boolean equals(ping_result that) {
      if (that == null)
        return false;

      boolean this_present_e = true && this.isSetE();
      boolean that_present_e = true && that.isSetE();
      if (this_present_e || that_present_e) {
        if (!(this_present_e && that_present_e))
          return false;
        if (!this.e.equals(that.e))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(ping_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      ping_result typedOther = (ping_result)other;

      lastComparison = Boolean.valueOf(isSetE()).compareTo(typedOther.isSetE());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetE()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.e, typedOther.e);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }

    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: // E
            if (field.type == org.apache.thrift.protocol.TType.STRUCT) {
              this.e = new DTException();
              this.e.read(iprot);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
      oprot.writeStructBegin(STRUCT_DESC);

      if (this.isSetE()) {
        oprot.writeFieldBegin(E_FIELD_DESC);
        this.e.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("ping_result(");
      boolean first = true;

      sb.append("e:");
      if (this.e == null) {
        sb.append("null");
      } else {
        sb.append(this.e);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
      // check for required fields
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
      try {
        write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
      try {
        read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

  }

}
