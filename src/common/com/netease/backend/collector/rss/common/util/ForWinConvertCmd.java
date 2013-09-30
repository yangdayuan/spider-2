package com.netease.backend.collector.rss.common.util;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.im4java.core.ConvertCmd;

public class ForWinConvertCmd extends ConvertCmd {

    public ForWinConvertCmd() {
        super();
        this.initForWin();
    }

    public ForWinConvertCmd(boolean useGM) {
        super(useGM);
        this.initForWin();
    }

    @SuppressWarnings("unchecked")
    protected void initForWin() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            try {
                Field field = this.getClass().getSuperclass().getSuperclass()
                        .getDeclaredField("iCommands");
                field.setAccessible(true);
                List<String> value = (List<String>) field.get(this);
                value.addAll(0, Arrays.asList(new String[]{"cmd","/C"}));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}