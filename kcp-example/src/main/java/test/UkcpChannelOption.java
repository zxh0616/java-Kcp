package test;

import io.netty.channel.ChannelOption;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public final class UkcpChannelOption<T> extends ChannelOption<T> {

    /**
     * 1、KCP_NODELAY 参数用于【设置是否启用低延迟模式】。如果将其设置为 1（true），则启用低延迟模式，KCP 将会以更低的延迟为代价来提高传输的实时性。在低延迟模式下，KCP 会更频繁地发送    数据包，并更积极地进行拥塞控制。
     * 2、如果将 UKCP_NODELAY 设置为 0（false），则禁用低延迟模式，KCP 将会以较高的延迟为代价来优化传输的吞吐量。在禁用低延迟模式下，KCP 会更加注重传输的稳定性和吞吐量，并会更智能地调整发送频率
     */
    public static final ChannelOption<Boolean> UKCP_NODELAY =
            valueOf(UkcpChannelOption.class, "UKCP_NODELAY");

    /**
     * 单位：毫秒
     * UKCP_INTERVAL 参数用于【设置 KCP 数据包的发送间隔】，即发送方每隔多长时间发送一个数据包。该值表示发送方每隔多少毫秒发送一个数据包到接收方。较小的发送间隔可以提高传输的实时性，但也会增加网络带宽的使用和CPU消耗。
     * 具体地说，UKCP_INTERVAL 参数控制了以下几个方面：
     * 1、数据包发送频率：较小的发送间隔意味着发送方更频繁地发送数据包到接收方。
     * 2、拥塞控制：较小的发送间隔可以更及时地感知网络拥塞情况，并采取相应的拥塞控制策略。
     * 3、数据传输实时性：较小的发送间隔可以提高数据传输的实时性，特别对于实时音视频传输等敏感应用场景。
     */
    public static final ChannelOption<Integer> UKCP_INTERVAL =
            valueOf(UkcpChannelOption.class, "UKCP_INTERVAL");

    /**
     * `UKCP_FAST_RESEND`参数用于【设置快速重传的阈值】。快速重传是一种优化技术，当接收方收到乱序的数据包时，它会尽早请求发送方重新发送丢失的数据包，以加速数据恢复。
     * `UKCP_FAST_RESEND` 参数表示当接收方收到对同一个数据包的第几个冗余 ACK（确认应答）时，就会立即请求发送方进行重传。较小的 UKCP_FAST_RESEND 值表示接收方会更早地进行快速重传请求。
     * 例如，如果 UKCP_FAST_RESEND 设置为 2，当接收方收到对同一个数据包的第二个冗余 ACK 时，就会立即请求发送方进行重传。
     * 快速重传是一种有效地拥塞控制策略，它可以更快地恢复丢失的数据包，提高传输性能和传输实时性。较小的 UKCP_FAST_RESEND 值可以更及时地触发快速重传，但也会增加网络带宽的使用，因为更频繁的重传请求会产生更多的网络流量。
     */
    public static final ChannelOption<Integer> UKCP_FAST_RESEND =
            valueOf(UkcpChannelOption.class, "UKCP_FAST_RESEND");


    /**
     * `UKCP_FAST_LIMIT` 参数用于【设置快速重传的最大重传次数限制】。当启用快速重传（通过设置 `UKCP_FAST_LIMIT` 参数为非零值）时，如果接收方收到了乱序的数据包，它会请求发送方进行重传。`UKCP_FAST_LIMIT` 参数表示快速重传的最大次数。当达到该次数后，接收方将不再请求发送方进行重传，而是等待原始地超时重传机制来进行丢包恢复。
     * 例如，如果 UKCP_FAST_LIMIT 设置为 10，那么当接收方对同一个数据包的快速重传次数达到 10 次时，将停止进一步的快速重传请求。
     */
    public static final ChannelOption<Integer> UKCP_FAST_LIMIT =
            valueOf(UkcpChannelOption.class, "UKCP_FAST_LIMIT");

    /*
    `UKCP_NOCWND` 参数【用于禁用拥塞窗口的调整】。当设置 `UKCP_NOCWND` 为 1 时，KCP 将不会进行拥塞窗口的调整，发送方将以最大传输速率发送数据，不会受到网络拥塞的影响。这可能会导致网络带宽的过度使用，并在网络拥塞时产生更多的丢包。

    通常情况下，禁用拥塞窗口并不是一个好的做法，因为它会导致网络拥塞，并可能对网络和其他应用产生负面影响。拥塞窗口在 TCP 中是一种重要的拥塞控制机制，它有助于平衡网络资源和保持网络的稳定性。
    */
    public static final ChannelOption<Boolean> UKCP_NOCWND =
            valueOf(UkcpChannelOption.class, "UKCP_NOCWND");

    /**
     * `UKCP_MIN_RTO` 参数【用于设置 KCP 的最小重传超时时间】。在 KCP 中，当发送方发送一个数据包后，会等待一个重传超时时间（RTO）来等待接收方的 ACK 确认。如果在 RTO 时间内没有收到 ACK，发送方会认为数据包丢失，然后进行重传。`UKCP_MIN_RTO` 参数表示 KCP 允许的最小 RTO 时间，即发送方的最小等待时间，以确保不会过于频繁地进行重传.
     */
    public static final ChannelOption<Integer> UKCP_MIN_RTO =
            valueOf(UkcpChannelOption.class, "UKCP_MIN_RTO");

    /**
     * 默认值是 1400 字节.
     * `UKCP_MTU` 参数用于【设置 KCP 的最大传输单元】。KCP 将根据该参数来调整数据包的大小，以确保数据包不会超过设定的 MTU 大小。通过设置合适的 `UKCP_MTU` 值，可以有效控制数据包的大小，减少数据包分片，从而优化网络传输性能.
     */
    public static final ChannelOption<Integer> UKCP_MTU =
            valueOf(UkcpChannelOption.class, "UKCP_MTU");

    /*  1、UKCP_RCV_WND 参数用于【指定 KCP 接收窗口的大小】，即在一个连续的传输会话中，允许发送方发送多少个数据包而不需要等待确认。接收窗口的大小直接影响了传输的吞吐量和延迟。
        2、较大的接收窗口可以提高传输的吞吐量，因为发送方可以连续发送多个数据包而不需要等待确认。然而，较大的接收窗口也会增加内存使用和丢包的风险，因为接收方需要维护一个更大的缓冲区来存储未确认的数据包。
        3、较小的接收窗口可以减少内存使用和丢包的风险，但可能会降低传输的吞吐量，因为发送方需要等待确认后才能继续发送数据包。
        4、在使用 KCP 时，根据网络环境和需求，您可以根据实际情况调整 UKCP_RCV_WND 参数的值来优化传输性能。通常，建议根据网络延迟、带宽和可用内存等因素来进行调整，以达到较好的传输效果。*/
    public static final ChannelOption<Integer> UKCP_RCV_WND =
            valueOf(UkcpChannelOption.class, "UKCP_RCV_WND");

    /**
     * `UKCP_SND_WND` 参数用于【设置 KCP 的发送窗口大小】，即发送方可以发送的未确认数据包的数量。较大的发送窗口可以提高传输的吞吐量，因为发送方可以连续发送多个数据包而不需要等待确认。然而，较大的发送窗口也会增加内存使用和丢包的风险，因为发送方需要维护一个更大的缓冲区来存储未确认的数据包.
     */
    public static final ChannelOption<Integer> UKCP_SND_WND =
            valueOf(UkcpChannelOption.class, "UKCP_SND_WND");

    /**
     * KCP 支持两种传输模式：消息模式(Message Mode）和流模式（Stream Mode）。
     * 在消息模式下，KCP 将数据包按消息进行传输，保证数据包按顺序传输，并且不会合并多个消息。这种模式适用于需要精确控制消息边界的场景，比如游戏中的操作指令。
     * 而在流模式下，KCP 将数据包视为连续的数据流进行传输，允许多个数据包合并在一起传输，从而提高传输的效率。这种模式适用于需要高吞吐量的场景，比如音视频传输。
     * `UKCP_STREAM` 参数用于【控制是否启用流模式传输】。当设置 `UKCP_STREAM` 为 1（true）时，KCP 将启用流模式传输。当设置为 0（false）时，KCP 将启用消息模式传输。
     */
    public static final ChannelOption<Boolean> UKCP_STREAM =
            valueOf(UkcpChannelOption.class, "UKCP_STREAM");


    /**
     * 当发送方发送一个数据包后，在规定的超时时间内，如果没有收到接收方的 ACK 确认，发送方会认为该数据包丢失，然后进行重传。这是 KCP 协议中重要的丢包恢复机制。
     * `UKCP_DEAD_LINK` 参数用于设置发送方对于未收到 ACK 确认的数据包的处理方式。具体来说，它表示发送方在多长时间内没有收到 ACK 确认后，会判定对应的数据包为死链（dead link），并终止对该数据包的重传.
     */
    public static final ChannelOption<Integer> UKCP_DEAD_LINK =
            valueOf(UkcpChannelOption.class, "UKCP_DEAD_LINK");

    /**
     * 在 KCP 协议中，发送方和接收方需要使用相同的 Conv 来建立一对一的通信。Conv 的作用是帮助 KCP 协议识别不同的会话，从而正确地将数据包交付给对应的会话。
     * `UKCP_AUTO_SET_CONV` 参数用于【控制是否自动设置会话编号】。当设置 UKCP_AUTO_SET_CONV 为 1（true）时，KCP 协议会自动设置 Conv，不需要应用层手动指定。当设置为 0（false）时，应用层需要手动指定 Conv，否则会导致数据包发送和接收不正确。
     * 自动设置 Conv 的优点是简化了应用层的工作，应用层不需要关心 Conv 的设置，KCP 协议会自动处理。这样可以减少应用层的复杂性和出错的可能性.
     */
    public static final ChannelOption<Boolean> UKCP_AUTO_SET_CONV =
            valueOf(UkcpChannelOption.class, "UKCP_AUTO_SET_CONV");

    /**
     * 在 KCP 协议中，发送方通常会按照一定的发送策略将数据包发送给接收方。当设置 `UKCP_FAST_FLUSH` 参数为 1（true）时，KCP 将启用快速发送模式。在快速发送模式下，发送方会尽快地将数据包发送给接收方，而不必等待发送窗口的确认。
     * 快速发送模式的优点是能够提高数据包的发送速度和传输效率，因为发送方不需要等待确认就可以立即发送数据包。这对于需要低延迟传输和实时性较高的应用场景非常有用，例如实时游戏和视频传输等。
     */
    public static final ChannelOption<Boolean> UKCP_FAST_FLUSH =
            valueOf(UkcpChannelOption.class, "UKCP_FAST_FLUSH");

    /**
     * KCP 协议支持将大的数据包拆分成多个较小的片段进行传输。接收方会将这些片段缓存起来，并在接收到所有片段后将它们合并还原成完整的数据包。这种拆分和合并的机制可以降低传输过程中的丢包率，并提高数据包的传输成功率。
     * `UKCP_MERGE_SEGMENT_BUF` 参数用于【控制是否合并数据包片段的缓冲区】。当设置 `UKCP_MERGE_SEGMENT_BUF` 为 1（true）时，KCP 将会开启合并数据包片段的缓冲区。这意味着接收方会缓存接收到的数据包片段，并在接收到所有片段后进行合并。这可以减少数据包的碎片化，提高数据包的传输成功率。
     */
    public static final ChannelOption<Boolean> UKCP_MERGE_SEGMENT_BUF =
            valueOf(UkcpChannelOption.class, "UKCP_MERGE_SEGMENT_BUF");

    @SuppressWarnings("deprecation")
    private UkcpChannelOption() {
        super(null);
    }

}
