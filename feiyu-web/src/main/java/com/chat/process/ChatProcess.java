package com.chat.process;

import com.alibaba.fastjson.JSON;
import com.chat.service.IChatService;
import com.common.ChatAnnotation;
import com.common.ICommonSockerProcess;
import com.common.WebSocketException;
import com.websocker.dto.ChatDTO;
import com.websocker.dto.WsBaseDTO;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Log4j2
public class ChatProcess extends ICommonSockerProcess  implements ApplicationContextAware {

    protected  static HashMap<String, IChatService> map = new HashMap<>();

    private static ApplicationContext applicationContext;
    @PostConstruct
    public void init (){
        Map<String, Object> annotation = applicationContext.getBeansWithAnnotation(ChatAnnotation.class);
        Set<Map.Entry<String, Object>> entries = annotation.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            if (entry.getValue() instanceof IChatService){
                IChatService obj = (IChatService)entry.getValue();
                String[] values = obj.getClass().getAnnotation(ChatAnnotation.class).value();
                for (String value : values) {
                    map.put(value,obj);
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext =applicationContext;
    }

    public static void chat(ChannelHandlerContext ctx, WsBaseDTO dto) throws WebSocketException {
        ChatDTO chatDTO = JSON.parseObject(dto.getMsgJson(), ChatDTO.class);
        if (chatDTO == null) {
            log.info("error chatDTO  chatDTO{}", chatDTO);
            throw new WebSocketException("聊天参数异常");
        }
        ChatProcess.map.get(chatDTO.getType()).issue(ctx, chatDTO);
    }

}