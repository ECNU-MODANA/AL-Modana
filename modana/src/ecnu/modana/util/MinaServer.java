package ecnu.modana.util;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Hashtable;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class MinaServer {
	public Hashtable<String, IoSession> toolFmuHt=new Hashtable<>();
	private int port=40001;
	public MinaServer(int port)
	{
		this.port=port;
	}
	public boolean Start(){
		try {
			// 创建服务端监控线程
	        IoAcceptor acceptor = new NioSocketAcceptor();
	        acceptor.getSessionConfig().setReadBufferSize(2048);
	        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
	        // 设置日志记录器
	        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
	        // 设置编码过滤器
	        acceptor.getFilterChain().addLast(
	            "codec",
	            new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
	        // 指定业务逻辑处理器
	        acceptor.setHandler(new TimeServerHandler());
	        // 设置端口号
	        acceptor.bind(new InetSocketAddress(port));
	        // 启动监听线程
	        acceptor.bind();
			return true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
	}
}
