package com.backblaze.erasure;

import com.backblaze.erasure.fecNative.FecDecode;
import com.backblaze.erasure.fecNative.FecEncode;
import com.backblaze.erasure.fecNative.ReedSolomonNative;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * Created by JinMiao
 * 2021/2/2.
 * FecAdapt 是 KCP 协议中的一个重要组件，用于进行前向纠错（Forward Error Correction，FEC）处理。FEC 是一种冗余编码技术，它在数据传输过程中增加冗余数据，以便在出现数据丢失或损坏时，可以通过冗余数据进行恢复，从而提高数据传输的可靠性。
 * 在 KCP 协议中，FecAdapt 负责处理 FEC 相关的逻辑。它根据数据包的情况动态调整 FEC 冗余数据的生成和使用，以适应不同的网络环境和数据丢失情况。
 * FecAdapt 主要有以下几个功能：
 * FEC 冗余数据的生成：根据需要，FecAdapt 可以生成冗余数据，并将其添加到发送的数据包中，用于前向纠错。
 * FEC 冗余数据的使用：在接收端，FecAdapt 可以根据收到的数据包和冗余数据，进行前向纠错，恢复丢失或损坏的数据包。
 * FEC 参数调整：FecAdapt 可以根据网络环境的变化和数据包的丢失情况，动态调整 FEC 冗余数据的生成和使用的策略，以优化传输性能。
 * FEC 状态维护：FecAdapt 维护了与 FEC 相关的状态信息，用于进行冗余数据的生成和使用。
 * 通过使用 FEC，KCP 协议可以在一定程度上提高数据传输的可靠性。当发生数据丢失时，接收端可以通过 FEC 冗余数据进行恢复，而不必等待数据包的重传，从而减少了传输延迟。这对于实时性要求较高的应用场景非常有用，例如实时音视频传输等。
 */


public class FecAdapt {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(FecAdapt.class);



    /**
     * ReedSolomonNative 是一个使用原生代码实现的 Reed-Solomon 算法类。Reed-Solomon 算法是一种前向纠错（Forward Error Correction，FEC）算法，用于在数据传输过程中添加冗余数据，以便在数据包丢失或损坏时可以通过冗余数据进行恢复，从而提高数据传输的可靠性。
     * ReedSolomonNative 类的构造函数接收两个参数 dataShards 和 parityShards，分别表示数据分片数和校验分片数。Reed-Solomon 算法通过将原始数据分成 dataShards 个数据分片和 parityShards 个校验分片，并根据这些分片生成冗余数据。
     */
    private ReedSolomonNative reedSolomonNative;
    private ReedSolomon reedSolomon;

    public FecAdapt(int dataShards, int parityShards){
        if(ReedSolomonNative.isNativeSupport()){
            reedSolomonNative = new ReedSolomonNative(dataShards,parityShards);
            log.info("fec use C native reedSolomon dataShards {} parityShards {}",dataShards,parityShards);
        }else{
            reedSolomon = ReedSolomon.create(dataShards,parityShards);
            log.info("fec use jvm reedSolomon dataShards {} parityShards {}",dataShards,parityShards);
        }

    }

    public IFecEncode fecEncode(int headerOffset,int mtu){
        IFecEncode iFecEncode;
        if(reedSolomonNative!=null){
            iFecEncode = new FecEncode(headerOffset,this.reedSolomonNative,mtu);
        }else{
            iFecEncode = new com.backblaze.erasure.fec.FecEncode(headerOffset,this.reedSolomon,mtu);
        }
        return iFecEncode;
    }


    public IFecDecode fecDecode(int mtu){
        IFecDecode iFecDecode;
        if(reedSolomonNative!=null){
            iFecDecode = new FecDecode(3*reedSolomonNative.getTotalShardCount(),this.reedSolomonNative,mtu);
        }else{
            iFecDecode = new com.backblaze.erasure.fec.FecDecode(3*this.reedSolomon.getTotalShardCount(),this.reedSolomon,mtu);
        }
        return iFecDecode;
    }
}
