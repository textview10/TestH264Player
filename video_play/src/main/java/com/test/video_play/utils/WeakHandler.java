package com.test.video_play.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 关于内存处理类
 * 在执行队列中，处理程序的原始实现始终保持对处理程序的硬引用。
 * 如果您创建匿名处理程序并将延迟消息插入其中，它将保持所有父类在记忆中的那个时间，即使它可以被清理。
 * 此实现更为棘手，它将对运行信息和消息保持弱引用，和GC可以收集它们一旦弱处理程序实例不再被引用。
 */
@SuppressWarnings("unused")
public class WeakHandler {
    private final Handler.Callback mCallback; // hard reference to Callback. We need to keep callback in memory
    private final ExecHandler mExec;
    private Lock mLock = new ReentrantLock();
    @SuppressWarnings("ConstantConditions")
    final ChainedRef mRunnables = new ChainedRef(mLock, null);

    /**
     * 如果该线程没有一个活套，这个处理程序将无法接收消息
     * 抛出异常。
     */
    public WeakHandler() {
        mCallback = null;
        mExec = new ExecHandler();
    }

    /**
     * 当前线程并采用回调接口，在该接口中可以处理消息
     * @param callback 回调接口，用于处理消息或空.
     */
    public WeakHandler(Handler.Callback callback) {
        mCallback = callback; // Hard referencing body
        mExec = new ExecHandler(new WeakReference<>(callback)); // Weak referencing inside ExecHandler
    }

    /**
     * 使用所提供的{Link Looper-}而不是默认的。
     *
     * @param looper 套环，不能为空。
     */
    public WeakHandler(Looper looper) {
        mCallback = null;
        mExec = new ExecHandler(looper);
    }

    /**
     * 调用回调处理消息的接口
     *
     * @param looper
     * @param callback 回调接口，用于处理消息或空 .
     */
    public WeakHandler(Looper looper, Handler.Callback callback) {
        mCallback = callback;
        mExec = new ExecHandler(looper, new WeakReference<>(callback));
    }

    /**
     * 使可运行R添加到消息队列中。
     * @param r
     * @return RealRealTrue如果RunnLabess成功放置到消息队列。在失败时返回false，
     * 通常是因为活套处理消息队列正在退出。
     */
    public final boolean post(Runnable r) {
        return mExec.post(wrapRunnable(r));
    }

    /**
     *  回调应该运行的绝对时间，使用时基，RealRealTrue如果RunnLabess成功放置到消息队列。
     *  在失败时返回false，通常是因为
     *  活套处理消息队列正在退出。注意真的结果并不意味着可运行的将被处理，
     *  在消息传递时间之前退出套接字发生，然后消息将被删除。
     */
    public final boolean postAtTime(Runnable r, long uptimeMillis) {
        return mExec.postAtTime(wrapRunnable(r), uptimeMillis);
    }


    public final boolean postAtTime(Runnable r, Object token, long uptimeMillis) {
        return mExec.postAtTime(wrapRunnable(r), token, uptimeMillis);
    }


    public final boolean postDelayed(Runnable r, long delayMillis) {
        return mExec.postDelayed(wrapRunnable(r), delayMillis);
    }


    public final boolean postAtFrontOfQueue(Runnable r) {
        return mExec.postAtFrontOfQueue(wrapRunnable(r));
    }

    /**
     * 删除消息队列中的RunnR R的任何挂起的帖子
     */
    public final void removeCallbacks(Runnable r) {
        final WeakRunnable runnable = mRunnables.remove(r);
        if (runnable != null) {
            mExec.removeCallbacks(runnable);
        }
    }


    public final void removeCallbacks(Runnable r, Object token) {
        final WeakRunnable runnable = mRunnables.remove(r);
        if (runnable != null) {
            mExec.removeCallbacks(runnable, token);
        }
    }


    public final boolean sendMessage(Message msg) {
        return mExec.sendMessage(msg);
    }


    public final boolean sendEmptyMessage(int what) {
        return mExec.sendEmptyMessage(what);
    }


    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        return mExec.sendEmptyMessageDelayed(what, delayMillis);
    }

    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        return mExec.sendEmptyMessageAtTime(what, uptimeMillis);
    }


    public final boolean sendMessageDelayed(Message msg, long delayMillis) {
        return mExec.sendMessageDelayed(msg, delayMillis);
    }


    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        return mExec.sendMessageAtTime(msg, uptimeMillis);
    }


    public final boolean sendMessageAtFrontOfQueue(Message msg) {
        return mExec.sendMessageAtFrontOfQueue(msg);
    }


    public final void removeMessages(int what) {
        mExec.removeMessages(what);
    }


    public final void removeMessages(int what, Object object) {
        mExec.removeMessages(what, object);
    }


    public final void removeCallbacksAndMessages(Object token) {
        mExec.removeCallbacksAndMessages(token);
    }


    public final boolean hasMessages(int what) {
        return mExec.hasMessages(what);
    }

    /**
     *检查是否有任何挂起邮件的帖子“代码”和“什么”，
     * 其对象是消息队列中的“对象”
     */
    public final boolean hasMessages(int what, Object object) {
        return mExec.hasMessages(what, object);
    }

    public final Looper getLooper() {
        return mExec.getLooper();
    }

    private WeakRunnable wrapRunnable(Runnable r) {
        //noinspection ConstantConditions
        if (r == null) {
            throw new NullPointerException("Runnable can't be null");
        }
        final ChainedRef hardRef = new ChainedRef(mLock, r);
        mRunnables.insertAfter(hardRef);
        return hardRef.wrapper;
    }

    private static class ExecHandler extends Handler {
        private final WeakReference<Callback> mCallback;

        ExecHandler() {
            mCallback = null;
        }

        ExecHandler(WeakReference<Callback> callback) {
            mCallback = callback;
        }

        ExecHandler(Looper looper) {
            super(looper);
            mCallback = null;
        }

        ExecHandler(Looper looper, WeakReference<Callback> callback) {
            super(looper);
            mCallback = callback;
        }

        @Override
        public void handleMessage(Message msg) {
            if (mCallback == null) {
                return;
            }
            final Callback callback = mCallback.get();
            if (callback == null) { // Already disposed
                return;
            }
            callback.handleMessage(msg);
        }
    }

    static class WeakRunnable implements Runnable {
        private final WeakReference<Runnable> mDelegate;
        private final WeakReference<ChainedRef> mReference;

        WeakRunnable(WeakReference<Runnable> delegate, WeakReference<ChainedRef> reference) {
            mDelegate = delegate;
            mReference = reference;
        }

        @Override
        public void run() {
            final Runnable delegate = mDelegate.get();
            final ChainedRef reference = mReference.get();
            if (reference != null) {
                reference.remove();
            }
            if (delegate != null) {
                delegate.run();
            }
        }
    }

    static class ChainedRef {
        ChainedRef next;
        ChainedRef prev;
        final Runnable runnable;
        final WeakRunnable wrapper;

        Lock lock;

        public ChainedRef(Lock lock, Runnable r) {
            this.runnable = r;
            this.lock = lock;
            this.wrapper = new WeakRunnable(new WeakReference<>(r), new WeakReference<>(this));
        }

        public WeakRunnable remove() {
            lock.lock();
            try {
                if (prev != null) {
                    prev.next = next;
                }
                if (next != null) {
                    next.prev = prev;
                }
                prev = null;
                next = null;
            } finally {
                lock.unlock();
            }
            return wrapper;
        }

        public void insertAfter(ChainedRef candidate) {
            lock.lock();
            try {
                if (this.next != null) {
                    this.next.prev = candidate;
                }

                candidate.next = this.next;
                this.next = candidate;
                candidate.prev = this;
            } finally {
                lock.unlock();
            }
        }

        public WeakRunnable remove(Runnable obj) {
            lock.lock();
            try {
                ChainedRef curr = this.next; // Skipping head
                while (curr != null) {
                    if (curr.runnable == obj) { // We do comparison exactly how Handler does inside
                        return curr.remove();
                    }
                    curr = curr.next;
                }
            } finally {
                lock.unlock();
            }
            return null;
        }
    }
}