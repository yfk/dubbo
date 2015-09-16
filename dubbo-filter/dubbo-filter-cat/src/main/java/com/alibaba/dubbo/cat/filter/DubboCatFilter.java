package com.alibaba.dubbo.cat.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

/**
 * Dubbo 加入Cat 监控
 * @author xiaomin.zhou
 *
 */
@Activate(group = {Constants.PROVIDER, Constants.CONSUMER})
public class DubboCatFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation invocation)
			throws RpcException {
		Transaction t = Cat.getProducer().newTransaction("Arch-RPC", getName(invoker, invocation));
		try {
			Result result = invoker.invoke(invocation);
			t.setStatus(Transaction.SUCCESS);
			return result;
		}
		catch (Throwable e) {
			Cat.getProducer().logError(e);
			t.setStatus(e);
			RpcException rpcException = new RpcException(e);
			throw rpcException;
		}
		finally {
			t.complete();
		}
	}

	private String getName(Invoker<?> invoker, Invocation invocation) {
		String name = invoker.getInterface().getName() + "::" +  invocation.getMethodName();
		return name;
	}
}
