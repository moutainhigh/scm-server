package org.trc.service.impl.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.trc.domain.util.Serial;
import org.trc.enums.CommonExceptionEnum;
import org.trc.enums.DistributeLockEnum;
import org.trc.enums.ExceptionEnum;
import org.trc.exception.ConfigException;
import org.trc.exception.RedisLockException;
import org.trc.mapper.util.ISerialMapper;
import org.trc.service.impl.BaseService;
import org.trc.service.util.ISerialUtilService;
import org.trc.util.AssertUtil;
import org.trc.util.CommonUtil;
import org.trc.util.SerialUtil;
import org.trc.util.lock.RedisLock;

import javax.annotation.Resource;

/**
 * Created by sone on 2017/5/8.
 */
@Service("serialUtilService")
public class SerialUtilService extends BaseService<Serial, Long> implements ISerialUtilService {

    private Logger  log = LoggerFactory.getLogger(SerialUtilService.class);

    @Resource
    private ISerialMapper iserialMapper;
    @Autowired
    private RedisLock redisLock;

    //获得流水号
    public int selectNumber(String name) {
          return  iserialMapper.selectNumber(name);
    }
    //获得前缀不固定的流水号
    public String generateRandomCode(int length,String flag,String ...names){ //需要其它的前缀，直接在后面添加
        int number = this.selectNumber(flag);//获得将要使用的流水号
        String code = SerialUtil.getMoveOrderNo(length,number,names);//获得需要的code编码
        int assess= this.updateSerialByName(flag,number);//修改流水的长度
        if (assess < 1) {
            String msg = CommonUtil.joinStr("保存编号数据库操作失败").toString();
            log.error(msg);
            throw new ConfigException(ExceptionEnum.DATABASE_SAVE_SERIAL_EXCEPTION, msg);
        }
        return code;
    }
    //获得前缀固定的流水号
    public String generateCode(int length,String ...names){ //需要其它的前缀，直接在后面添加
        AssertUtil.notEmpty(names, "生成序列号传入的序列号生成规则名称不能为空");
        String code = "";
        String lockKey = DistributeLockEnum.SERIAL_GENERATE.getCode() + "serialGenerate-"+names[0];
        String identifier = redisLock.Lock(lockKey, 5000, 2000);
        if (StringUtils.isBlank(identifier)){
            throw new RedisLockException(CommonExceptionEnum.REDIS_LOCK_ERROR, String.format("序列号%s生成失败", names[0]));
        }
        try{
            int number = this.selectNumber(names[0]);//获得将要使用的流水号
            code = SerialUtil.getMoveOrderNo(length,number,names);//获得需要的code编码
            int assess= this.updateSerialByName(names[0],number);//修改流水的长度
            if (assess < 1) {
                String msg = CommonUtil.joinStr("保存编号数据库操作失败").toString();
                log.error(msg);
                throw new ConfigException(ExceptionEnum.DATABASE_SAVE_SERIAL_EXCEPTION, msg);
            }
        }catch (Exception e){
            log.error(String.format("序列号%s生成异常", names[0]), e);
        }finally {
            //释放锁
            if (redisLock.releaseLock(lockKey, identifier)) {
                log.info("锁" +lockKey + "已释放！");
            } else {
                log.error("锁" +lockKey + "解锁失败！");
            }
        }
        return code;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public int updateSerialByName(String name,int number) {
        int countVersionChange = iserialMapper.updateSerialVersionByName(name, number,number-1 );
        if (countVersionChange == 0) {
            String msg = CommonUtil.joinStr("流水的版本[vesionMark=", number + "", "]的数据已存在,请再次提交").toString();
            throw new ConfigException(ExceptionEnum.DATABASE_DATA_VERSION_EXCEPTION, msg);
    }
        return countVersionChange;

    }

}
