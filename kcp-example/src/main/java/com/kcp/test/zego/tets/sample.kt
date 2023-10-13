//package com.kcp.test.test//package test
//
//import android.app.Application
//import android.util.Log
//import com.loostone.thirdparty.sdkmanage.R
//import im.zego.zegoexpress.ZegoExpressEngine
//import im.zego.zegoexpress.callback.IZegoEventHandler
//import im.zego.zegoexpress.constants.*
//import im.zego.zegoexpress.entity.*
//import org.json.JSONObject
//import java.nio.ByteBuffer
//
//data class ZegoRoom(
//        val roomId: String = "",
//        val userId: String = "",
//        val isHost: Boolean = false
//)
//
//object ZegoManager {
//
//    private lateinit var engine: ZegoExpressEngine
//    private var zegoRoom = ZegoRoom()/
//    private val zegoAudioFrameParam = ZegoAudioFrameParam().apply {
//        this.sampleRate = ZegoAudioSampleRate.ZEGO_AUDIO_SAMPLE_RATE_44K
//    }
////    private val eventHandler = object: IZegoEventHandler() {
////        /** 以下为常用的房间相关回调  */
////        /** 房间状态更新回调  */
////        override fun onRoomStateChanged(
////                roomID: String,
////                reason: ZegoRoomStateChangedReason,
////                errorCode: Int,
////                extendedData: JSONObject
////        ) {
////            Log.e("lsktv", "zego on room state changed roomId [$roomID], error code [$errorCode], reason [$reason]")
////            if (reason == ZegoRoomStateChangedReason.LOGINING) {
////                // 登录中
////            } else if (reason == ZegoRoomStateChangedReason.LOGINED) {
////                // 登录成功
////                //只有当房间状态是登录成功或重连成功时，推流（startPublishingStream）、拉流（startPlayingStream）才能正常收发音视频
////                //将自己的音视频流推送到 ZEGO 音视频云
////            } else if (reason == ZegoRoomStateChangedReason.LOGIN_FAILED) {
////                // 登录失败
////            } else if (reason == ZegoRoomStateChangedReason.RECONNECTING) {
////                // 重连中
////            } else if (reason == ZegoRoomStateChangedReason.RECONNECTED) {
////                // 重连成功
////            } else if (reason == ZegoRoomStateChangedReason.RECONNECT_FAILED) {
////                // 重连失败
////            } else if (reason == ZegoRoomStateChangedReason.KICK_OUT) {
////                // 被踢出房间
////            } else if (reason == ZegoRoomStateChangedReason.LOGOUT) {
////                // 登出成功
////            } else if (reason == ZegoRoomStateChangedReason.LOGOUT_FAILED) {
////                // 登出失败
////            }
////        }
////
////        /** 用户状态更新  */
////        override fun onRoomUserUpdate(
////                roomID: String?,
////                updateType: ZegoUpdateType?,
////                userList: ArrayList<ZegoUser?>?
////        ) {
////            /** 根据需要实现事件回调  */
////            Log.e("lsktv", "zego on room user state updated roomId [$roomID], updateType [$updateType]")
////        }
////
////        /** 流状态更新  */
////        override fun onRoomStreamUpdate(
////                roomID: String?,
////                updateType: ZegoUpdateType?,
////                streamList: ArrayList<ZegoStream?>?,
////                extendedData: JSONObject?
////        ) {
////            /** 根据需要实现事件回调  */
////            Log.e("lsktv", "zego on room stream state updated roomId [$roomID]")
////        }
////
////        /** 常用的推流相关回调  */
////        /** 推流状态更新回调  */
////        override fun onPublisherStateUpdate(
////                streamID: String,
////                state: ZegoPublisherState,
////                errorCode: Int,
////                extendedData: JSONObject
////        ) {
////            /** 根据需要实现事件回调  */
////            Log.e("lsktv", "zego on publisher state changed roomId [$streamID], error code [$errorCode], reason [$state]")
////        }
////
////        /** 常用的拉流相关回调  */
////        /** 拉流状态相关回调  */
////        override fun onPlayerStateUpdate(
////                streamID: String,
////                state: ZegoPlayerState,
////                errorCode: Int,
////                extendedData: JSONObject
////        ) {
////            /** 根据需要实现事件回调  */
////            Log.e("lsktv", "zego on player state changed streamID [$streamID], error code [$errorCode], reason [$state]")
////        }
////
////        override fun onPublisherQualityUpdate(
////                streamID: String?,
////                quality: ZegoPublishStreamQuality?
////        ) {
////            super.onPublisherQualityUpdate(streamID, quality)
////            quality?.let {
////                Log.e("lsktv", "onPublisherQualityUpdate level: [${quality.level}]," +
////                        "audioKBPS: [${quality.audioKBPS}], " +
////                        "audioSendBytes: [${quality.audioSendBytes}]," +
////                        "rtt: [${quality.rtt}]," +
////                        "packetLostRate: [${quality.packetLostRate}]," +
////                        "totalSendBytes: [${quality.totalSendBytes}]")
////            }
////        }
////
////        override fun onPlayerQualityUpdate(streamID: String?, quality: ZegoPlayStreamQuality?) {
////            super.onPlayerQualityUpdate(streamID, quality)
////            quality?.let {
////                Log.e("lsktv", "onPlayerQualityUpdate level: [${quality.level}]," +
////                        "audioKBPS: [${quality.audioKBPS}], " +
////                        "audioRecvBytes: [${quality.audioRecvBytes}]," +
////                        "rtt: [${quality.rtt}]," +
////                        "packetLostRate: [${quality.packetLostRate}]," +
////                        "totalSendBytes: [${quality.totalRecvBytes}]")
////            }
////        }
////
////    }
//
//    fun init(application: Application) {
//        val profile = ZegoEngineProfile()
//        /** 请通过官网注册获取，格式为 123456789L */
//        profile.appID = application.resources.getString(R.string.zego_sdk_appid).toLong()
//
//        /** 请通过官网注册获取，格式为：@"0123456789012345678901234567890123456789012345678901234567890123"（共64个字符）*/
//        profile.appSign = application.resources.getString(R.string.zego_sdk_appsign)
//
//        /** 指定使用直播场景 (请根据实际情况填写适合你业务的场景) */
//        profile.scenario = ZegoScenario.KARAOKE
//
//        /** 设置app的application 对象 */
//        profile.application = application
//
//        /** 创建引擎 */
//        engine = ZegoExpressEngine.createEngine(profile, eventHandler)
//        engine.enableAGC(true)
//        engine.enableAEC(true)
//        engine.enableANS(true)
//
//        val audioConfig = ZegoCustomAudioConfig()
//        audioConfig.sourceType = ZegoAudioSourceType.CUSTOM
//        engine.enableCustomAudioIO(true, audioConfig)
//    }
//
//    fun startConnecting(zegoRoom: ZegoRoom, handler: (Boolean) -> Unit) {
//        Log.e("lsktv", "zego start connecting room [${zegoRoom.roomId}]")
//
//        ZegoManager.zegoRoom = zegoRoom
//
//        // ZegoUser 的构造方法 public ZegoUser(String userID) 会将 “userName” 设为与传的参数 “userID” 一样。“userID” 与 “userName” 不能为 “null” 否则会导致登录房间失败。
//        val user = ZegoUser(zegoRoom.userId)
//        // 只有传入 “isUserStatusNotify” 参数取值为 “true” 的 ZegoRoomConfig，才能收到 onRoomUserUpdate 回调。
//        val roomConfig = ZegoRoomConfig()
//        // 如果您使用 appsign 的方式鉴权，token 参数不需填写；如果需要使用更加安全的 鉴权方式： token 鉴权，请参考[如何从 AppSign 鉴权升级为 Token 鉴权](https://doc-zh.zego.im/faq/token_upgrade?product=ExpressVideo&platform=all)
//        // roomConfig.token = "xxxx";
//        roomConfig.isUserStatusNotify = true
//        // 登录房间
//        engine.loginRoom(zegoRoom.roomId, user, roomConfig) { errorCode: Int, _: JSONObject? ->
//            Log.e("lsktv", "zego sdk loginRoom result $errorCode")
//            handler(errorCode == 0)
//            if (errorCode != 0) {
//
//            }
//        }
//
//    }
//
//    fun startStream() {
//        Log.e("lsktv", "zego start streaming")
//        // 推流
//        engine.startPublishingStream(publishingStreamId())
//        // 拉流
//        engine.startPlayingStream(playingStreamId())
//    }
//
//    private fun publishingStreamId(): String = String.format("stream-%s-%s", zegoRoom.roomId, if (zegoRoom.isHost) "00" else "11" )
//    private fun playingStreamId(): String = String.format("stream-%s-%s", zegoRoom.roomId, if (zegoRoom.isHost) "11" else "00" )
//
//    fun sendAudioPcm(data: ByteBuffer, dataLength: Int) {
////        Log.e("lsktv", "zego send pcm $dataLength")
//        engine.sendCustomAudioCapturePCMData(data, dataLength, zegoAudioFrameParam)
//    }
//
//    fun fetchAudioPcm(data: ByteBuffer, dataLength: Int) {
////        Log.e("lsktv", "zego fetch pcm $dataLength")
//        try {
//            engine.fetchCustomAudioRenderPCMData(data, dataLength, zegoAudioFrameParam)
//        } catch (e: Throwable) {
//            Log.e("lsktv", "fetchAudioPcm $e")
//        }
////        Log.e("lsktv", "zego fetch pcm end")
//    }
//
//    fun stopStream() {
//        Log.e("lsktv", "zego stop stream")
//        /** 停止推流 */
//        engine.stopPublishingStream()
//        /** 停止拉流 */
//        engine.stopPlayingStream(playingStreamId())
//    }
//
//    fun logoutRoom() {
//        Log.e("lsktv", "zego login out room [${zegoRoom.roomId}]")
//        engine.logoutRoom(zegoRoom.roomId)
//        zegoRoom = ZegoRoom()
//    }
//
//}
