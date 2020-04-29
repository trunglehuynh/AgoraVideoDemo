package com.agora.videodemo;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public interface RtcProvider {
    RtcEngine getRtcEngine(IRtcEngineEventHandler iRtcEngineEventHandler);
}
