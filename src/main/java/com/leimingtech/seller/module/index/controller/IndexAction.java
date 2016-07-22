package com.leimingtech.seller.module.index.controller;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.leimingtech.core.auth.shiro.UsernamePasswordToken;
import com.leimingtech.core.captcha.RandCodeImageUtils;
import com.leimingtech.core.common.SpringContextUtil;
import com.leimingtech.core.entity.Order;
import com.leimingtech.core.entity.base.Consult;
import com.leimingtech.core.entity.base.EvaluateStore;
import com.leimingtech.core.entity.base.Goods;
import com.leimingtech.core.entity.base.Member;
import com.leimingtech.core.entity.base.Store;
import com.leimingtech.core.entity.base.StoreGrade;
import com.leimingtech.core.entity.vo.ArticleClassTitleVo;
import com.leimingtech.core.entity.vo.StoreVo;
import com.leimingtech.core.jackson.JsonUtils;
import com.leimingtech.core.state.goods.GoodsState;
import com.leimingtech.seller.test.Group;
import com.leimingtech.seller.test.ReadArea;
import com.leimingtech.seller.utils.Util;
import com.leimingtech.seller.utils.sessionKey.CacheUtils;
import com.leimingtech.service.module.goods.service.GoodsService;
import com.leimingtech.service.module.member.service.MemberService;
import com.leimingtech.service.module.store.service.EvaluateStoreService;
import com.leimingtech.service.module.store.service.StoreGradeService;
import com.leimingtech.service.module.store.service.StoreService;
import com.leimingtech.service.module.trade.service.ConsultService;
import com.leimingtech.service.module.trade.service.OrderService;
import com.leimingtech.service.module.website.service.ArticleClassService;



/**
 * 首页
 * 项目名称：leimingtech-seller   
 * 类名称：IndexAction   
 * 类描述：   
 * 创建人：liuhao   
 * 创建时间：2014年11月25日 下午9:07:10   
 * 修改人：liuhao   
 * 修改时间：2014年11月25日 下午9:07:10   
 * 修改备注：   
 * @version    
 *
 */
@Controller
@RequestMapping("/")
@Slf4j
public class IndexAction {

	String message = "success";

	@Resource
    private EvaluateStoreService evaluateStoreService;
    @Resource
    private ArticleClassService articleClassService;
    @Resource
    private StoreService storeService;
    @Resource
    private ConsultService consultService;
    @Resource
    private OrderService orderService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private StoreGradeService storeGradeService;//店铺等级
    /**
     * 导航主页面跳转
     * @param model
     * @return
     */
	@RequestMapping("")
	public String  index(Model model){
        //跳转地址
        String str="";
        StoreVo store=null;
        Store storebymember=storeService.findByMemberId(CacheUtils.getCacheUser().getMember().getMemberId());
        if(storebymember!=null){
        	store=storeService.findVoById(storebymember.getStoreId());
        }
		try{
            if(store != null && store.getStoreState()==1){
            	CacheUtils.initCacheUser(CacheUtils.getCacheUser().getMember().getMemberName().trim());
                model.addAttribute("store",store);
                model.addAttribute("evaluateStore",evaluateStoreService.findEvaluateStore
                        (store.getStoreId()));
                Goods goods = new Goods();
                goods.setStoreId(store.getStoreId());
                /**
                 * 仓库、下架、定时上架的商品
                 * goods_state 0
                 * goods_show  0
                 */
        		goods.setGoodsState(GoodsState.GOODS_OPEN_STATE);
        		goods.setGoodsShow(GoodsState.GOODS_STORE_SHOW);
                model.addAttribute("publishCount", goodsService.countGoods(goods));
                
                /**
                 * 上架的商品
                 * goods_state 0
                 * goods_show  1
                 */
        		goods.setGoodsState(GoodsState.GOODS_OPEN_STATE);
        		goods.setGoodsShow(GoodsState.GOODS_ON_SHOW);
                model.addAttribute("onlineCount",goodsService.countGoods(goods));
                
                /**
                 * 下架的商品
                 * goods_state 1
                 * goods_show  0
                 */
                goods.setGoodsState(GoodsState.GOODS_CLOSE_STATE);
        		goods.setGoodsShow(GoodsState.GOODS_OFF_SHOW);
                model.addAttribute("lockupCount",goodsService.countGoods(goods));
                
                Consult consult = new Consult();
                consult.setStoreId(store.getStoreId());
                consult.setReplyStatus(1);
                model.addAttribute("consultCount",consultService.findCount(consult));
                
                Order order = new Order();
                
                /**
                 * 店铺中所有订单
                 */
                order.setStoreId(store.getStoreId());
                model.addAttribute("allorderCount", orderService.findOrderCount(order));
                /**
                 * 待付款
                 */
               // order.setStoreId(CacheUtils.getCacheUser().getStore().getStoreId());
                order.setOrderState(10);
                model.addAttribute("noPayment", orderService.findOrderCount(order));
                
                /**
                 * 待发货
                 */
                order.setOrderState(20);
                model.addAttribute("noDelivery",orderService.findOrderCount(order));
                
                /**
                 * 已发货
                 */
                order.setOrderState(30);
                model.addAttribute("waitDelivery",orderService.findOrderCount(order));
                /**
                 * 待评价
                 */
                order.setEvaluationStatus(0);//待评价
                order.setOrderState(40);
                model.addAttribute("waitEvaluate",orderService.findOrderCount(order));
              
                
                /**
                 * 已完成
                 */
                order.setOrderState(40);
                model.addAttribute("finshlivery",orderService.findOrderCount(order));
                List<ArticleClassTitleVo> listArticleClassTitleVo = articleClassService.findTitleList();
                //评分
                EvaluateStore evaluateStore=null;
        		if(store.getStoreId()!=0){
        			evaluateStore=evaluateStoreService.findEvaluateStore(store.getStoreId());
        			//保留一位小数
        			NumberFormat numberFormat= NumberFormat.getNumberInstance() ; 
        			numberFormat.setMaximumFractionDigits(1);
        			if(evaluateStore!=null){
        				//发货速度评分
        				if(evaluateStore.getSevalDeliverycredit()!=null){
        					evaluateStore.setSevalDeliverycredit(Double.valueOf(numberFormat.format(evaluateStore.getSevalDeliverycredit())));
        				}
        				//描述相符评分
        				if(evaluateStore.getSevalDesccredit()!=null){
        					evaluateStore.setSevalDesccredit(Double.valueOf(numberFormat.format(evaluateStore.getSevalDesccredit())));
        				}
        				//服务态度评分
        				if(evaluateStore.getSevalServicecredit()!=null){
        					evaluateStore.setSevalServicecredit(Double.valueOf(numberFormat.format(evaluateStore.getSevalServicecredit())));
        				}
        			}
        			 if(!evaluateStore.getCount().equals("0")){
        			    evaluateStore.setAverageCredit(Util.getAverageCreditFormat(evaluateStore));
        			 }
        		}
        		StoreGrade storeGrade=storeGradeService.queryById(Long.valueOf(store.getGradeId()));
        		model.addAttribute("evaluateStore", evaluateStore);
                model.addAttribute("listArticleClassTitleVo", listArticleClassTitleVo);
                model.addAttribute("store", store);
                model.addAttribute("member", CacheUtils.getCacheUser().getMember());
                model.addAttribute("storeGrade", storeGrade);
                str="/index/index";
            }else{
            	model.addAttribute("store",store);
            	str="/index/step1";
            }
			return str;
		}catch (Exception e) {
			e.printStackTrace();
			log.error("卖家中心首页加载失败！");
			throw new RuntimeException("导航失败!");
		}
	}
	
	
	/**
	 * 默认加载的中心页面
	 * @Title: main 
	 * @param @return    设定文件 
	 * @return ModelAndView    返回类型 
	 * @throws
	 */
	@RequestMapping(value = "content")
	public ModelAndView content(){
		ModelAndView model = new ModelAndView("/index/index_content");
		return model;
	}

	/**
	 * 跳转至登陆页面
	 * @return
	 */
	@RequestMapping(value = "login")
	public String login() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {

            return "redirect:/";
        }
        return "/index/login";
	}

	/**
	 * 生成验证码图片io流
	 */
	@RequestMapping(value = "generateImage", method = RequestMethod.GET)
	public void generateImage(HttpServletResponse response,HttpServletRequest request)
			throws ServletException, IOException {
        /*Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        String captcha =  PatchcaUtils.generateImage(response);
        session.setAttribute("captcha", captcha);*/
        RandCodeImageUtils.generateImage(response, request);
	}

	/**
	 * 用户登录操作
	 * @return 登录后的地址
	 */
    @RequestMapping(value = "loginCheck", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> loginCheck(@RequestParam(value="username") String username,
						@RequestParam(value="password") String password,
						@RequestParam(value="captcha") String captcha,
						@RequestParam(value ="remember_me",required = false) String remember_me,
                        Model model, HttpServletRequest request) {
        String error = null;
		Subject subject = SecurityUtils.getSubject();
		Session session = subject.getSession();
		String sessionCaptcha = (String) session.getAttribute("captcha");
		Map<String,Object> map = Maps.newHashMap();
		if(StringUtils.isNotEmpty(username)){//判断店铺是否关闭
			Store store=oncheck(username);
//			if(store!=null&&store.getStoreState()==0){
//				map.put("success",false);
//				map.put("message", "该店铺已关闭!");
//				return map;
//			}
		}
		if (!sessionCaptcha.equals(captcha.trim().toUpperCase())) {
			map.put("success",false);
			map.put("message", "验证码错误");
			return map;
			//return map;
			//model.addAttribute("error", error);
			//return "/index/login";
		}
		boolean isrmemberName = "1".equals(remember_me) ? true : false;
        UsernamePasswordToken token = new UsernamePasswordToken(username, password.toCharArray(), isrmemberName, "", captcha, false);
		try {
			if ("1".equals(remember_me)) {
				token.setRememberMe(true);
			}
			subject.login(token);
            CacheUtils.initCacheUser(subject.getPrincipal().toString());
//            if(CacheUtils.getCacheUser().getStore()!=null&&CacheUtils.getCacheUser().getStore().getStoreState()==0){//判断店铺是否关闭
//				map.put("success",false);
//				map.put("message", "该店铺已关闭!");
//				return map;
//			}
//            String ip = IpUtil.getIpAddr(request);
        } catch (UnknownAccountException e) {
			//error = "用户名/密码错误";
        	map.put("success",false);
			map.put("message", "用户名/密码错误");
			return map;
		} catch (IncorrectCredentialsException e) {
			error = "用户名/密码错误";
			map.put("success",false);
			map.put("message", "用户名/密码错误");
			return map;
		} catch (Exception e) {
			//其他错误，比如锁定，如果想单独处理请单独catch 处理
			map.put("success",false);
			map.put("message", "其他错误:"+e.getMessage());
			error = "其他错误：" + e.getMessage();
			return map;
		}
//		 if(CacheUtils.getCacheUser().getStore()!=null&&CacheUtils.getCacheUser().getStore().getStoreState()==0){//判断店铺是否关闭
//				map.put("success",false);
//				map.put("message", "该店铺已关闭!");
//				return map;
//		}
		if(CacheUtils.getCacheUser().getStore()!=null){
				//修改店铺的上次登陆时间
				storeService.updateStore(CacheUtils.getCacheUser().getStore().getStoreId());
		}
		map.put("success",true);
		map.put("message","登陆成功");
		return map;
		//return "redirect:/";
	}

	/**
	 * 用户登出操作
	 * @return
	 */
	@RequestMapping(value = "logout", method = RequestMethod.GET)
	public String logout() {
		Subject subject = SecurityUtils.getSubject();
		subject.logout();
        CacheUtils.cleanCacheUser();
		return "redirect:/";
	}

	@RequestMapping(value = "createArea")
	public String createArea(Model model) throws IOException {
		List<Group> groups= ReadArea.read();
		model.addAttribute("groups", groups);
		System.out.println("===================");
		System.out.println(JsonUtils.toJsonStr(groups));
		return "index/test/area";
	}
	
	
	/**
	 * 没有权限
	 * @return
	 */
	@RequestMapping("/joinIn/unauthor")
	public String unauthor() {
		
		return "/index/unauthor";
	}
	
	public Store oncheck(String membername){
		MemberService memberService = SpringContextUtil.getBean(MemberService.class);
		StoreService storeService = SpringContextUtil.getBean(StoreService.class);
		Member member = memberService.findMemberByName(membername);
		Store store=null;
		if(member!=null){
			store = storeService.findByMemberId(member.getMemberId());
		}
		return store;
	}
}