package com.jarvis.cache.aop.aspectj;

import com.jarvis.cache.CacheHandler;
import com.jarvis.cache.annotation.Cache;
import com.jarvis.cache.annotation.CacheDelete;
import com.jarvis.cache.annotation.CacheDeleteTransactional;
import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * 使用Aspectj 实现AOP拦截 注意：拦截器不能有相同名字的Method
 *
 * 入口方法  这个可以参考 https://github.com/qiujiayu/cache-example/blob/master/src/main/resources/applicationContext.xml 的配置
 *
 * 下面的类被用于处理aop切面
 */
public class AspectjAopInterceptor {

    private final CacheHandler cacheHandler;

    public AspectjAopInterceptor(CacheHandler cacheHandler) {
        this.cacheHandler = cacheHandler;
    }

    //-------------------------- 拦截mybatis的mapper

    /**
     * 拦截cache
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    public Object checkAndProceed(ProceedingJoinPoint pjp) throws Throwable {
        Signature signature = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method.isAnnotationPresent(Cache.class)) {
            Cache cache = method.getAnnotation(Cache.class);// method.getAnnotationsByType(Cache.class)[0];
            return this.proceed(pjp, cache);
        }

        try {
            return pjp.proceed();
        } catch (Throwable e) {
            throw e;
        }
    }

    /**
     *
     * 删除cache
     *
     * @param jp
     * @param retVal
     * @throws Throwable
     */
    public void checkAndDeleteCache(JoinPoint jp, Object retVal) throws Throwable {
        Signature signature = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method.isAnnotationPresent(CacheDelete.class)) {
            CacheDelete cacheDelete = method.getAnnotation(CacheDelete.class);
            this.deleteCache(jp, cacheDelete, retVal);
        }
    }

    public Object checkAndDeleteCacheTransactional(ProceedingJoinPoint pjp) throws Throwable {
        Signature signature = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method.isAnnotationPresent(CacheDeleteTransactional.class)) {
            CacheDeleteTransactional cache = method.getAnnotation(CacheDeleteTransactional.class);// method.getAnnotationsByType(Cache.class)[0];
            return this.deleteCacheTransactional(pjp, cache);
        }
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            throw e;
        }
    }

    //------------------------------------- @cache 连接方法

    /**
     * 拦截cache  拦截方式  around
     */
    public Object proceed(ProceedingJoinPoint aopProxyChain, Cache cache) throws Throwable {
        return cacheHandler.proceed(new AspectjCacheAopProxyChain(aopProxyChain), cache);
    }

    /**
     *
     * 拦截删除cache    连接方式：after-retuning
     *
     * @param aopProxyChain
     * @param cacheDelete
     * @param retVal
     * @throws Throwable
     */
    public void deleteCache(JoinPoint aopProxyChain, CacheDelete cacheDelete, Object retVal) throws Throwable {
        cacheHandler.deleteCache(new AspectjDeleteCacheAopProxyChain(aopProxyChain), cacheDelete, retVal);
    }

    public Object deleteCacheTransactional(ProceedingJoinPoint aopProxyChain,
                                           CacheDeleteTransactional cacheDeleteTransactional) throws Throwable {
        return cacheHandler.proceedDeleteCacheTransactional(
                new AspectjDeleteCacheTransactionalAopProxyChain(aopProxyChain), cacheDeleteTransactional);
    }

    public CacheHandler getCacheHandler() {
        return cacheHandler;
    }

}
