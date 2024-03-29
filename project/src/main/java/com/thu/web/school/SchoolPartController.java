package com.thu.web.school;//school;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.thu.domain.*;
import com.thu.service.*;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.security.MessageDigest;
import java.util.*;
import io.jsonwebtoken.Claims;


import javax.xml.rpc.ServiceException;


import static com.thu.domain.QResponse.response;

/**
 * Created by hetor on 16/12/2.
 */
@RestController
//如果都有误，那么就返回{'success':bool, 'msg':''}
public class SchoolPartController {


    @Autowired
    Jwt jwtService;
    @Autowired
    ResponseService responseService;
    @Autowired
    UserService userService;
    @Autowired
    RoleService roleService;
    @Autowired
    QuestionService questionService;

    TokenMap tokenMap = new TokenMap();

    public static final String xiaoban="xiaoban";
    public static final String zongban="zongban";
    private final String roleZB = "zongban";
    private final String roleXB = "xiaoban";
    private final String roleALL = "all";
    //TODO: 后勤部门约定KEY
    private final static String KEY = "sdf!2l12@#2dsfDFSDF";
//    @RequestMapping(value = "/test/login",method = RequestMethod.POST)
//    public  String Login(@RequestBody)

    public static final String Error_Msg="{\"success\":false,\"msg\":\"Invalid Token\"}";
    public static final String Erro_Parse="{\"success\":false,\"msg\":\"Invalid Question_id\"}";
    public static final String Erro_Responder="{\"success\":false,\"msg\":\"Invalid Responder\"}";
    public static final String Erro_Response="{\"success\":false,\"msg\":\"Invalid Responce\"}";
    public static final String Erro_Role="{\"success\":false,\"msg\":\"Invalid Role\"}";
    public static final String Erro_User="{\"success\":false,\"msg\":\"Invalid TUser\"}";
    public static final String Erro_DDL="{\"success\":false,\"msg\":\"Invalid DeadLine\"}";
    public static final String Erro_TIMESTAMP1="{\"success\":false,\"msg\":\"Invalid TIMESTAMP1\"}";
    public static final String Erro_TIMESTAMP2="{\"success\":false,\"msg\":\"Invalid TIMESTAMP2\"}";
    public static final String Erro_TIMESTAMP3="{\"success\":false,\"msg\":\"Invalid TIMESTAMP3\"}";
    public static final String Erro_Status="{\"success\":false,\"msg\":\"Wrong Question Status\"}";
    public static final String Erro_Zongban="{\"success\":false,\"msg\":\"Wrong Zongban interface\"}";

    //判断是否是主责部分
    private boolean checkMain(String role){

        if (role.equals(roleXB)||role.equals(roleZB))
            return true;
        else
            return false;
    }

    //后勤部门MD5验证
    private static String MD5(String sourceStr) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes("utf-8"));
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
        } catch (Exception e) {}
        return result.toUpperCase();
    }

    private  boolean checkPermission(String token){
        String tokenRole, tokenName;
        try{
            Claims claims = jwtService.parseToken(token);
            tokenRole = claims.get("role").toString();
            tokenName = claims.get("uname").toString();
        }catch (Exception e){
            return false;
        }

        String checkToken = tokenMap.getToken(tokenName);
        if (checkToken == ""||checkToken==null||!checkToken.equals(token)){
            return false;
        }

        return true;
    }


    public boolean CheckToken(String token){



        return checkPermission(token);
    }
    public String getRole(String token){
        if(!CheckToken(token))
            return null;
        String tokenRole=null;
        try{
            Claims claims = jwtService.parseToken(token);
            tokenRole = claims.get("role").toString();
//            tokenName = claims.get("uname").toString();
        }
        catch (Exception e){
            return null;
        }
        return tokenRole;

    }
    public String getUsername(String token){
//        public static String getRole(String token){
        if(!CheckToken(token))
            return null;
        String tokenName=null;
        try{
            Claims claims = jwtService.parseToken(token);
//                tokenRole = claims.get("role").toString();
            tokenName = claims.get("uname").toString();
        }
        catch (Exception e){
            return null;
        }
        return tokenName;

//        }
    }


    //主责部门获取问题列表
//  return:  [{'question_id':'', 'created_time':'', 'timestamp1':'', 'timestamp2':'',
//            'timestamp3':'' 'status':'', 'resp_role':'',
//            'is_common':bool, 'content':'', 'delay_days':number, 'delay_reason':'',
//            'is_common_top':bool, 'reclassify_reason':'', 'created_location':'', 'likes':int}]
//    public static Map<String,String> token_role;
    public String Convert(Object o){
        if(o!=null)
            return o.toString();
        return null;
    }

    @RequestMapping(value = "/questions/get_all/{token:.+}",method = RequestMethod.POST)
    public String getMainQuestions(@RequestParam(name = "start") int start,@RequestParam(name="number") int number ,@PathVariable String token){

//        System.out.println(token);
//        if(true)
//            return "ok";
//        token_role.containsKey()

        if(!CheckToken(token))
            return Error_Msg;
        String role=getRole(token);
        if(role==null)
            return Erro_Role;
        List<Question> questions=new ArrayList<>();
        if(role.equals(xiaoban)){
            questions= questionService.getAllQuestions();          //questionRepository.getQuestions();
        }else if(role.equals(zongban)){
            questions=questionService.getAllQuestionsForRole(roleService.findByRole(role));  //questionRepository.getQuestionsbyRole(roleReposiroty.findRole(role));
        }else{
            questions=questionService.getQuestionForRelatedRole(roleService.findByRole(role));    //questionRepository.getQuestionbyRela(roleReposiroty.findRole(role));
//            return Erro_Role;
        }
        JSONArray ja=new JSONArray();

        if(start<0||number<0||questions==null){
            return Error_Msg;
        }

        for(int i=start;i<start+number&&i<questions.size();i++){
            Question question=questions.get(i);
            JSONObject result= new JSONObject();
            result.put("question_id",this.Convert(question.getQuestionId()));
            result.put("created_time",this.Convert(question.getCreatedTime()));//.toString());
            result.put("created_location",question.getCreatedLocation());
            result.put("timestamp1",this.Convert(question.getTimestamp1()));//.toString());
            result.put("timestamp2",this.Convert(question.getTimestamp2()));//.toString());
            result.put("timestamp3",this.Convert(question.getTimestamp3()));//.toString());
            result.put("status",question.getStatus().ordinal());


//            result.put("")
//            result.put("resp_role_name",question.getLeaderRole().getDisplayName());
            List<String> pic_name=new ArrayList<>();
            List<Pic> pics=question.getPics();
            if(pics!=null) {
                for (Pic pic : pics) {
                    pic_name.add(pic.getPath());
                }
            }
            result.put("pic_path",pic_name);

            List<String> role_role=new ArrayList<>();
            List<String> role_res_name=new ArrayList<>();

            Role transferRole = question.getTransferRole();
            if(transferRole != null)
            {
                role_role.add(transferRole.getRole());
                role_res_name.add(transferRole.getDisplayName());
            }

            Role lead_role=question.getLeaderRole();
            if(lead_role!=null){
                role_res_name.add(lead_role.getDisplayName());
                role_role.add(lead_role.getRole());
            }
//            role_role.add(lead_role.);
            List<Role> other_roles=question.getOtherRoles();
            if(other_roles!=null&&other_roles.size() != 0){
                for(Role oother:other_roles){
                    role_res_name.add(oother.getDisplayName());
                    role_role.add(oother.getRole());
                }
            }



            result.put("resp_role",role_role);
            result.put("resp_role_name",role_res_name);

            result.put("title",question.getTitle());
            result.put("content",question.getContent());
            result.put("deadline",this.Convert(question.getDdl()));//.toString());

            result.put("likes",question.getLikes());
            result.put("opinion",question.getInstruction());
            result.put("is_common",question.getCommon());
            result.put("content",question.getContent());
            result.put("delay_days",question.getDelayDays());
            result.put("delay_reason",question.getDelayReason());
            result.put("is_common_top",question.getCommonTop());
            result.put("reclassify_reason",question.getReclassifyReason());
            result.put("created_location",question.getCreatedLocation());
            result.put("created_location",question.getLikes());
            EvaluationType type_eval=question.getEvaluationType();
            int score=0;
            if(type_eval==EvaluationType.UNSATISFIED)
                score=-1;
            else if(type_eval==EvaluationType.SATISFIED)
                score=1;
            result.put("student_score",score);
            result.put("student_comment",question.getEvaluationDetail());
            JSONArray ja_response=new JSONArray();
            List<Response> null_responses=question.getResponses();
            if(null_responses!=null) {
                for (Response response : null_responses) {
                    JSONObject jo = new JSONObject();
                    jo.put("response_id", response.getResponseId());
                    jo.put("response_content", response.getResponseContent());
                    jo.put("time", response.getRespondTime().toLocalDate());
                    ja_response.add(jo);
                }
            }
            result.put("responses",ja_response);
            ja.add(result);
        }
        return ja.toString();



//        TUser hah = userRepository.findUser("hah");
//        return "[{'question_id':'question_id', 'created_time':'created_time', 'timestamp1':'timestamp1', 'timestamp2':'timestamp2', 'timestamp3':'timestamp3' 'status':'status', 'resp_role':'resp_role',"+
//            "'is_common':false, 'content':'content', 'delay_days':3, 'delay_reason':'delay_reason'," +
//            "'is_common_top':false, 'reclassify_reason':'reclassify_reason', 'created_location':'created_location', 'likes':20}]";
    }

    //主责部门直接回复
    // {'success':bool, 'msg':''}
    @RequestMapping(value = "/questions/main/response/{token:.+}",method = RequestMethod.POST)
    public String MainResponse(@RequestParam(name = "question_id") String q_id,@RequestParam("response_content") String content,@PathVariable String token){
        content=StringEscapeUtils.escapeHtml(content);
        token=StringEscapeUtils.escapeHtml(token);
        if(!CheckToken(token))
            return Error_Msg;
        Long qid=Long.parseLong("-1");
        try{
            qid= Long.parseLong(q_id);
        }catch (Exception e){
            return Erro_Parse;
        }
        //产生一条回复
        String role=getRole(token);
        if(!role.equals(xiaoban))
            return Erro_Role;
//        if(role==null)
//            return Erro_Role;
        String username=getUsername(token);
        if(username==null)
            return Erro_User;
        TUser responder= userService.findUser(username);           //userRepository.findUserbyName(username);
        if(responder==null)
            return Erro_Responder;
        if(content==null||content.equals(""))
            return Erro_Response;
        Response respon= responseService.respond(content,responder);//                 responseRepository.insertResponse(content,responder,new Date());
        if(respon==null)
            return Erro_Response;
        //将回复插入到问题中
        Boolean insertResponse= questionService.responsibleDeptRespond(qid,respon);      //questionRepository.responsebyMain(qid,respon,respon.getRespondTime());
//        Boolean updateReceivedNumber=
        if(insertResponse) {
            Boolean updateRole= roleService.updateNumber(xiaoban,Long.parseLong("1"),Long.parseLong("1"),null,Long.parseLong("1"));
            if(!updateRole)
                return Erro_Role;

            questionService.modifyUnreadQuestions(qid, true);
            return "{\"success\":true, \"msg\":\"main response ok\"}";
        }
        else
            return "{\"success\":false, \"msg\":\"main response failure\"}";
    }

    //主责部门直接拒绝
    //{'success':bool, 'msg':''}
    @RequestMapping(value = "/questions/main/reject/{token:.+}",method = RequestMethod.POST)
    public String MainReject(@RequestParam("question_id") String q_id,@PathVariable String token) {
        if(!CheckToken(token))
            return Error_Msg;
        Long qid=Long.parseLong("-1");
        try{
            qid= Long.parseLong(q_id);
        }catch (Exception e){
            return Erro_Parse;
        }
        String content = "问题未通过审核";
        Boolean reject_ok=   questionService.responsibleDeptReject(qid,content);                      //questionRepository.rejectbyMain(qid,content,new Date());
        if(reject_ok) {
            Boolean updateRole= roleService.updateNumber(xiaoban,Long.parseLong("1"),null,null,null);
            if(!updateRole)
                return Erro_Role;
            questionService.modifyUnreadQuestions(qid, true);
            return "{\"success\":true, \"msg\":\"main reject ok\"}";
        }
        else
            return "{\"success\":false, \"msg\":\"main reject failure\"}";
    }

    //主责部门转发问题
    //{'success':bool, 'msg':''}
    @RequestMapping(value = "/questions/main/forward/{token:.+}",method = RequestMethod.POST)
    public String MainForward(@RequestParam("question_id")String q_id,@RequestParam("role") String for_role,@PathVariable String token) {
        if(!CheckToken(token))
            return Error_Msg;

        Long qid=Long.parseLong("-1");
        try{
            qid= Long.parseLong(q_id);
        }catch (Exception e){
            return Erro_Parse;
        }
        String role=getRole(token);
        if(role==null)
            return Erro_Role;
        Role forword_role=   roleService.findByRole(for_role);        //roleReposiroty.findRole(for_role);
        if(forword_role==null)
            return Erro_Role;
        Boolean transfer_ok=  questionService.transferQuestion(qid,forword_role);            //questionRepository.transferbyMain(qid,forword_role,role,new Date());
        if(transfer_ok) {
            //更新forword_role表的信息
            //更新timestamp1
            if(role.equals(xiaoban)) {
                Boolean setTimeStamp1 = questionService.updateTimestamp(qid, LocalDateTime.now(), null, null);
                if (!setTimeStamp1)
                    return Erro_TIMESTAMP1;
            }else{
                return Erro_Role;
            }

            if (for_role.equals(zongban)) {
                //TODO: 调用后勤部门接口
                //http://wx.93001.cn/services/wsActionPort.jws?wsdl
                //{"sign":"69CE72B0563413496361234E3FE4D137","img_url":["http://www.baidu.com/1.jpg","http://www.baidu.com/2.jpg","http://www.baidu.com/3.jpg"],"content":"荷塘月色很漂亮","id":12345,"person":"史蒂夫","title":"美丽清华开发测试","contact":"13487452376","deadLine":"2017-03-10 10:10:10"}
                System.out.println("start forward to zongban!");
                String sign = MD5(KEY + qid + KEY);
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("sign",sign);
                jsonObject.put("id",qid);
                Question question=questionService.findById(qid);
                if(question==null)
                    return Erro_Zongban;
                TUser user_stu=question.getTUser();
                jsonObject.put("title",question.getTitle());
                jsonObject.put("content",question.getContent());
                List<Pic> pictures=question.getPics();
                List<String> pic_url=new ArrayList<>();
                if(pictures==null)
                    return Erro_Zongban;
                for(Pic pic:pictures){
                    pic_url.add(pic.getPath());
                }

                jsonObject.put("img_url",pic_url);
                jsonObject.put("person",user_stu.getUname());
                jsonObject.put("contact", "unknown");
                LocalDateTime localDateTime=LocalDateTime.now().plusDays(3);
                questionService.modifyDDL(qid,localDateTime);
                jsonObject.put("deadLine",this.Convert(localDateTime));
                String url="http://wx.93001.cn/services/wsActionPort.jws?wsdl";
                try {
                    Service service = new Service();
                    Call call = null;
                    try {
                        call=(Call) service.createCall();
                    } catch (ServiceException e) {
                        return Erro_Zongban;
                    }
                    call.setTargetEndpointAddress(url);
                    call.setOperationName("receive");//WSDL里面描述的接口名称
                    call.addParameter("json", XMLType.XSD_STRING,
                            javax.xml.rpc.ParameterMode.IN);//接口的参数
                    call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);//设置返回类型
                    String temp = jsonObject.toString();
                    System.out.println(temp);
                    String result = (String)call.invoke(new Object[]{temp});
                    //给方法传递参数，并且调用方法
                    System.out.println("forward result is "+result);
                    if(!result.equals("success")){
                        return Erro_Zongban;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return Erro_Zongban;
                }

//                String time=localDateTime.getYear()+"-"+localDateTime.get
            }

            questionService.modifyUnreadQuestions(qid, true);
            return "{\"success\":true, \"msg\":\"main transfer ok\"}";
        }
        else
            return "{\"success\":false, \"msg\":\"main transfer failure\"}";
    }

    @RequestMapping(value = "/questions/zongban/response",method = RequestMethod.POST)
    public String ZBResponse(
            @RequestParam("id") String q_id,
            @RequestParam("role") String lead_role,
            @RequestParam("content") String r_content,
            @RequestParam("sign") String sign
    ) {
        //TODO:后勤部门结果回复
        //1. MainClassify()
        //@RequestParam("question_id") String q_id,
        //@RequestParam("leader_role") String lead_role,
        //@RequestParam("other_roles") String other_roles, 无
        //@RequestParam("deadline") String ddl, 无，在MainForward分给总办时设为默认的三天
        //@RequestParam("opinion") String opinion, 无
//        String pp=MD5(KEY+Long.parseLong(q_id)+KEY);
        Long qid=Long.parseLong("-1");
        try{
            qid= Long.parseLong(q_id);
        }catch (Exception e){
            return Erro_Parse;
        }
        Question question=questionService.findById(qid);
        if(question==null)
            return "failure";
        String pp=MD5(KEY+qid+KEY);
        Role leader= roleService.findByRole(lead_role);         //roleReposiroty.findRole(lead_role);
        if(leader==null)
            return "failure";
//        Role leader=roleService
        if(pp.equals(sign)) {
//            String re1 = MainClassify(q_id, lead_role, null, null, null, null);
            //2. RelaResponse()
            //@RequestParam("response_content") String r_content
            boolean re1=questionService.classifyQuestion(qid,leader,null,question.getDdl(),question.getInstruction());


            TUser responder=  userService.findUser(lead_role);     //userRepository.findUserbyName(username);
            if(responder==null)
            {
                return "failure";
            }

            if(r_content==null||r_content.equals(""))
                return "failure";
            Response respon= responseService.respond(r_content,responder);           //responseRepository.insertResponse(r_content,responder,new Date());
            if(respon==null)
                return "failure";
            //将回复插入到问题中
            boolean insertResponse= questionService.responsibleDeptRespond(qid,respon);     //questionRepository.responsebyRela(qid,respon);

//            boolean re2=questionService.responsibleDeptRespond(qid,r_content);
            if(re1&&insertResponse)
                return "success";
            else
                return "failure";

        }
        else {
            return "failure";
        }
    }


    /*    /quesitons/main/reclassify
   //    send: {'question_id':'', 'agree':bool}
   //    receive: {'success':bool, 'msg':''}
   */
    @RequestMapping(value = "/questions/main/reclassify/{token:.+}",method = RequestMethod.POST)
    public String MainReclassify(@RequestParam("question_id") String q_id,@RequestParam("agree") boolean agree,@PathVariable String token)
    {
        if(!CheckToken(token))
            return Error_Msg;

        Long qid=Long.parseLong("-1");
        try{
            qid= Long.parseLong(q_id);
        }catch (Exception e){
            return Erro_Parse;
        }
        String role=getRole(token);
        if(role==null)
            return Erro_Role;
        //
        Role forword_role=   roleService.findByRole(xiaoban);
        boolean reclassify_ok=  questionService.ReclassifyQuestion(qid,agree,forword_role);
        if(reclassify_ok) {
            questionService.modifyUnreadQuestions(qid, true);
            return "{\"success\":true, \"msg\":\"main agree/refuse reclassify ok\"}";
        }
        else
            return "{\"success\":false, \"msg\":\"main agree/refuse reclassify failure\"}";

    }

    /**
     * /questions/main/delay
     send: {'question_id':'', 'agree':bool}
     receive: {'success':bool, 'msg':''}
     * @param q_id
     * @param agree
     * @param token
     * @return
     */
    @RequestMapping(value = "/questions/main/delay/{token:.+}",method = RequestMethod.POST)
    public String MainDelay(@RequestParam("question_id") String q_id,@RequestParam("agree") boolean agree,@PathVariable String token) {
        if (!CheckToken(token))
            return Error_Msg;

        Long qid = Long.parseLong("-1");
        try {
            qid = Long.parseLong(q_id);
        } catch (Exception e) {
            return Erro_Parse;
        }
        String role = getRole(token);
        if (role == null)
            return Erro_Role;
        boolean delay_ok=  questionService.DelayQuestion(qid,agree);
        if(delay_ok) {
            questionService.modifyUnreadQuestions(qid, true);
            return "{\"success\":true, \"msg\":\"main agree/refuse delay ok\"}";
        }
        else
            return "{\"success\":false, \"msg\":\"main agree/refuse delay failure\"}";
    }


//    @RequestMapping(value = "/questions/main/delay",method = RequestMethod.POST)
//    public String MainDelay(@RequestParam("question_id") String q_id){
//        return "";
//    }

    //主责部门的对问题分类
    //{'success':bool, 'msg':''}
    @RequestMapping(value = "/questions/main/classify/{token:.+}",method = RequestMethod.POST)
    public String MainClassify(@RequestParam("question_id") String q_id,
                               @RequestParam("leader_role") String lead_role,
                               @RequestParam("other_roles") String other_roles,
                               @RequestParam("deadline") String ddl,
                               @RequestParam("opinion") String opinion,@PathVariable String token)
	{

        if(!CheckToken(token))
            return Error_Msg;
        Long qid=Long.parseLong("-1");
        try{
            qid= Long.parseLong(q_id);
        }catch (Exception e){
            return Erro_Parse;
        }
        String role=getRole(token);
        if(role==null)
            return Erro_Role;
        Role leader= roleService.findByRole(lead_role);         //roleReposiroty.findRole(lead_role);
        if(leader==null)
            return Erro_Role;
        List<Role> others = new ArrayList<>();
        if (other_roles!=null&&other_roles != "") {
            String[] _other_roles = other_roles.split(",");
            for(String role_str:_other_roles){
                Role oth= roleService.findByRole(role_str) ;   //.findRole(role_str);
                if(oth==null)
                    return Erro_Role;
                others.add(oth);
            }
        }
//        SimpleDateFormat sdf  =   new  SimpleDateFormat( "yyyy-MM-dd" );
//        Date date= new Date();
//        try {
//            date= sdf.parse(ddl);
//        } catch (ParseException e) {
//            return Erro_DDL;
//        }
        LocalDateTime ldt = LocalDateTime.now();
        if(ddl!=null) {
            String[] timeArray = ddl.split("-");
            int[] times = new int[6];
            if (timeArray.length < 3 || timeArray.length > 6)
                return Erro_DDL;
            else {
                for (int i = 0; i < 6; i++) {
                    if (i < timeArray.length) {
                        try {
                            times[i] = Integer.parseInt(timeArray[i]);
                        } catch (Exception e) {
                            return Erro_Parse;
                        }
                    } else
                        times[i] = 0;
                }
            }


            try {
                ldt = LocalDateTime.of(times[0], times[1], times[2], times[3], times[4], times[5]);
            } catch (Exception e) {
                return Erro_DDL + ";" + Erro_Parse;
            }
        }else{
            ldt=questionService.findById(qid).getDdl();
        }

        boolean classfy_ok=  questionService.classifyQuestion(qid,leader,others,ldt,opinion);     // (qid,leader,others,date,opinion,role);           //questionRepository.classifybyMain(qid,leader,others,date,opinion,role,new Date());
        boolean setTimeStamp=false;
        if(role.equals(xiaoban)){
            setTimeStamp=questionService.updateTimestamp(qid,LocalDateTime.now(),null,null);
            if(!setTimeStamp)
                return Erro_TIMESTAMP1;
        }else if(role.equals(zongban)){
            setTimeStamp=questionService.updateTimestamp(qid,null,LocalDateTime.now(),null);
            if(!setTimeStamp)
                return Erro_TIMESTAMP2;
        }else{
            return Erro_Role;
        }

        if(classfy_ok && setTimeStamp) {
            boolean updateRole= roleService.updateNumber(lead_role,Long.parseLong("1"),null,null,null); //roleReposiroty.updateReceivedNumber(lead_role,Long.parseLong("1"));
            if(!updateRole)
                return Erro_Role;
            questionService.modifyUnreadQuestions(qid, true);
            return "{\"success\":true, \"msg\":\"main classify ok\"}";
        }
        else
            return "{\"success\":false, \"msg\":\"main classify failure\"}";
    }

    //相关部门获取问题列表
//    [{'question_id':'', 'created_time':'', 'deadline':'',
//            'created_location':'', 'status':'', 'content':'', 'likes':number,
//            'is_read':bool, 'is_common':bool, 'is_common_top':bool}]
    @RequestMapping(value = "/questions/related/get_all/{token:.+}",method = RequestMethod.POST)
    public String getRelateQuestions(@RequestParam(name = "start") int start,@RequestParam(name="number") int number ,@PathVariable String token){
        if(!CheckToken(token))
            return Error_Msg;

        String role=getRole(token);
        if(role==null)
            return Erro_Role;
        Role rela_role=roleService.findByRole(role);
        if(null==rela_role)
            return Erro_Role;
        List<Question> questions=questionService.getQuestionForRelatedRole(rela_role);         //questionRepository.getQuestionbyRela(rela_role);

        JSONArray ja=new JSONArray();
        if(start<0||number<0||questions==null){
            return Error_Msg;
        }

        for(int i=start;i<start+number&&i<questions.size();i++){
            Question question=questions.get(i);
            JSONObject result= new JSONObject();
            result.put("question_id",question.getQuestionId().toString());
            result.put("created_time",question.getCreatedTime().toString());
            result.put("timestamp1",question.getTimestamp1().toString());
            result.put("timestamp2",question.getTimestamp2().toString());
            result.put("timestamp3",question.getTimestamp3().toString());
            result.put("status",question.getStatus().toString());
            result.put("resp_role",question.getLeaderRole().getRole());
            result.put("is_common",question.getCommon());
            result.put("content",question.getContent());
            result.put("delay_days",question.getDelayDays());
            result.put("delay_reason",question.getDelayReason());
            result.put("is_common_top",question.getCommonTop());
            result.put("reclassify_reason",question.getReclassifyReason());
            result.put("created_location",question.getCreatedLocation());
            result.put("likes",question.getLikes());
            ja.add(result);
        }
        return ja.toString();
//        return "[{'question_id':'question_id', 'created_time':'created_time', 'deadline':'deadline',"+
//            "'created_location':'created_location', 'status':'', 'content':'content', 'likes':23,"+
//            "'is_read':false, 'is_common':false, 'is_common_top':false}]";
    }

    //相关部门获取具体信息
//    {'opinion':'', 'pic_path':['',''],
//        'responses':[{'response_id':'', 'response_content':''}]}
    @RequestMapping(value = "/questions/related/get_detail/{token:.+}",method = RequestMethod.POST)
    public String getRelaQueDetail(@RequestParam(name="question_id") String q_id,@PathVariable String token){
        if(!CheckToken(token))
            return Error_Msg;
        Long qid=Long.parseLong("-1");
        try{
            qid= Long.parseLong(q_id);
        }catch (Exception e){
            return Erro_Parse;
        }

//        String role=getRole(token);
//        List<Question> questions=null;
        Question question= questionService.getQuestionDetail(qid);//       questionRepository.getQuestionbyId(qid);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("opinion",question.getInstruction());

        List<Pic> pics=question.getPics();
        List<String> pics_path=new ArrayList<>();
        for(Pic pic:pics){
            pics_path.add(pic.getPath());
        }
        jsonObject.put("pic_path",pics_path);
        JSONArray ja_response=new JSONArray();
        for(Response response:question.getResponses()){
            JSONObject jo=new JSONObject();
            jo.put("response_id",response.getResponseId());
            jo.put("response_content",response.getResponseContent());
            ja_response.add(jo);
        }
        jsonObject.put("responses",ja_response);
        return jsonObject.toString();
//        return "{'opinion':'opinion', 'pic_path':['pic_path_0','pic_path_1'],"+
//        "'responses':[{'response_id':'response_id', 'response_content':'response_content'}]}";
    }

    //相关部门申请重分类
//    {'success':bool, 'msg':''}
    @RequestMapping(value = "/questions/related/reclassify/{token:.+}",method = RequestMethod.POST)
    public String RelaReclassify(@RequestParam("question_id") String q_id,@RequestParam("reclassify_reason") String reason,@PathVariable String token){
        if(!CheckToken(token))
            return Error_Msg;
        Long qid=Long.parseLong("-1");
        try{
            qid= Long.parseLong(q_id);
        }catch (Exception e){
            return Erro_Parse;
        }
        Role tuan= roleService.findByRole(xiaoban);
        String cur_role=getRole(token);
        if(cur_role==null)
            return Erro_Role;
        if(null==tuan)
            return Erro_Role;
        boolean require_reclassify= questionService.applyReclassifyQuestion(qid,reason,tuan);  //questionRepository.reclassifybyRela(qid,reason,tuan);
        if(require_reclassify) {
            boolean updateRole =  roleService.updateNumber(cur_role,Long.parseLong("-1"),null,null,null);  //roleReposiroty.updateReceivedNumber(cur_role, Long.parseLong("-1"));
            if (!updateRole)
                return Erro_Role;
            questionService.modifyUnreadQuestions(qid, true);
            return "{\"success\":true, \"msg\":\"require have been send successfully\"}";
        }
        else
            return "{\"success\":false, \"msg\":\"require have not been send\"}";
    }

//    @RequestMapping(value = "/questions/related/delay",method = RequestMethod.POST)
//    public String RelaDelay(@RequestParam("question_id") String q_id,@RequestParam("delay_reason") String reason,@RequestParam("delay_days") int days){
//        return "";
//    }

    //相关部门的回复（包括追加回复）
//    {'success':bool, 'msg':''}
    @RequestMapping(value = "/questions/related/response/{token:.+}",method = RequestMethod.POST)
    public String RelaResponse(@RequestParam("question_id") String q_id,@RequestParam("response_content") String r_content,@PathVariable String token){
        r_content= StringEscapeUtils.escapeHtml(r_content);
        token=StringEscapeUtils.escapeHtml(token);
        if(!CheckToken(token))
            return Error_Msg;
        Long qid=Long.parseLong("-1");
        try{
            qid= Long.parseLong(q_id);
        }catch (Exception e){
            return Erro_Parse;
        }

        Status status_qu=questionService.getQuestionDetail(qid).getStatus();
        if(status_qu!=Status.UNSOLVED&&status_qu!=Status.SOLVING&&status_qu!=Status.SOLVED){
            return Erro_Status;
        }
        List<Response> has_responses= questionService.getQuestionDetail(qid).getResponses();         //questionRepository.getQuestionbyId(qid).getResponses();
        LocalDateTime ddl=questionService.getQuestionDetail(qid).getDdl();      //getQuestionDetail(qid).getDdl();
        //生成一条回复
        String username=getUsername(token);
        if(username==null)
            return Erro_User;
        String role=getRole(token);
        if(role==null)
            return Erro_Role;
        TUser responder=  userService.findUser(username);     //userRepository.findUserbyName(username);
        if(responder==null)
            return Erro_Responder;

        if(r_content==null||r_content.equals(""))
            return Erro_Response;
        Response respon= responseService.respond(r_content,responder);           //responseRepository.insertResponse(r_content,responder,new Date());
        if(respon==null)
            return Erro_Response;
        //将回复插入到问题中
        boolean insertResponse= questionService.responsibleDeptRespond(qid,respon);     //questionRepository.responsebyRela(qid,respon);
        if(insertResponse) {
            if(has_responses.size()==0) {
                if(respon.getRespondTime().compareTo(ddl)>0)
                {
                    boolean updateRole = roleService.updateNumber(role,null,null,Long.parseLong("1"),null); //roleReposiroty.updateOvertimeNumber(role, Long.parseLong("1"));
                    if (!updateRole)
                        return Erro_Role;
                }else{
                    boolean updateRole = roleService.updateNumber(role,null,Long.parseLong("1"),null,null);
                    if (!updateRole)
                        return Erro_Role;
                }

            }
            questionService.modifyUnreadQuestions(qid, true);
            return "{\"success\":true, \"msg\":\"respond successfully\"}";
        }
        else
            return "{\"success\":false, \"msg\":\"respond unsuccessfully\"}";
    }

    //相关部门修改回复
//    {'success':bool, 'msg':''}
    @RequestMapping(value = "/questions/related/modify_response/{token:.+}",method = RequestMethod.POST)
    public String ModifyResponse(@RequestParam("question_id") String q_id,@RequestParam("response_id") String r_id,@RequestParam("response_content") String r_content,@PathVariable String token){
        r_content=StringEscapeUtils.escapeHtml(r_content);
        if(!CheckToken(token))
            return Error_Msg;
        Long qid=Long.parseLong("-1");
        try{
            qid= Long.parseLong(q_id);
        }catch (Exception e){
            return Erro_Parse;
        }
        Long rid=Long.parseLong("-1");
        try{
            rid= Long.parseLong(r_id);
        }catch (Exception e){
            return Erro_Parse;
        }
        Status status_qu=questionService.getQuestionDetail(qid).getStatus();
        if(status_qu!=Status.SOLVING&&status_qu!=Status.SOLVED){
            return Erro_Status;
        }

        boolean modify_ok=  responseService.editResponse(rid,r_content);      //responseRepository.modifyResponse(rid,r_content,new Date());
        if(modify_ok) {
            questionService.modifyUnreadQuestions(qid, true);
            return "{\"success\":true, \"msg\":\"modify response successfully\"}";
        }
        else
            return "{\"success\":false, \"msg\":\"modify response unsuccessfully\"}";

    }



    }
