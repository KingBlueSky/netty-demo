package com.netty.demo.wechat.demo.server.handler;

import com.netty.demo.wechat.demo.procotol.request.CreateGroupRequestPacket;
import com.netty.demo.wechat.demo.procotol.response.CreateGroupResponsePacket;
import com.netty.demo.wechat.demo.util.IDUtil;
import com.netty.demo.wechat.demo.util.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.DefaultChannelGroup;

import java.util.ArrayList;
import java.util.List;

@ChannelHandler.Sharable
public class CreateGroupRequestHandler extends SimpleChannelInboundHandler<CreateGroupRequestPacket> {

    public static final CreateGroupRequestHandler INSTANCE = new CreateGroupRequestHandler();

    private CreateGroupRequestHandler() {}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CreateGroupRequestPacket createGroupRequestPacket) throws Exception {

        List<String> userIdList = createGroupRequestPacket.getUserIdList();
        List<String> userNameList = new ArrayList<>();

        // 1.创建channel分组
        DefaultChannelGroup channelGroup = new DefaultChannelGroup(ctx.executor());

        // 2.筛选出待加入群聊用户的channel和userName
        for (String userId : userIdList) {
            Channel channel = SessionUtil.getChannel(userId);

            if (channel != null) {
                channelGroup.add(channel);
                userNameList.add(SessionUtil.getSession(channel).getUserName());
            }
        }

        String groupId = IDUtil.randomUserId();
        SessionUtil.bindChannelGroup(groupId, channelGroup);

        // 3.创建群聊的响应
        CreateGroupResponsePacket createGroupResponsePacket = new CreateGroupResponsePacket();
        createGroupResponsePacket.setSuccess(true);
        createGroupResponsePacket.setUserNameList(userNameList);
        createGroupResponsePacket.setGroupId(groupId);

        // 4.给每个客户端发送拉群通知
        channelGroup.writeAndFlush(createGroupResponsePacket);

        System.out.println("群组创建成功， id为【" + createGroupResponsePacket.getGroupId() + "】");
        System.out.println("群里有：【" + createGroupResponsePacket.getUserNameList() + "】");
    }
}
