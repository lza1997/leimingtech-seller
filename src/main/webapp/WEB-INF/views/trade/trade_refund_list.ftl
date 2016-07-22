<@p.header title="商家中心-退款记录"/>
<script type="text/javascript" src="${base}/res/js/common_select.js"
	charset="utf-8"></script>
<script type="text/javascript"
	src="${base}/res/js/ajaxfileupload/ajaxfileupload.js"></script>
<script type="text/javascript" src="${base}/res/js/layer/layer.js" charset="utf-8"></script>
<script type="text/javascript" src="${base}/res/js/My97DatePicker/WdatePicker.js" charset="utf-8"></script>	 
<div class="layout">
	<div class="sidebar"></div> 
	<div class="right-content">
        <div class="path">
      		<div>
      			<a href="#">商家中心</a><span></span>
                <a href="#"/>退款记录</a><span>
                </span>退款记录</div>
    	</div>
        <div class="main">
			<div class="wrap">
  				<div class="tabmenu">
    				<ul class="tab pngFix">
  						<li class="active"><a  href="#">退款记录</a></li>
  					</ul>
  				</div>
	  			<form method="post" action="${base}/trade/refundOrderList" id="queryForm">
	    			<table class="search-form">
	      				<tr>
	        				<td>&nbsp;</td>
	      					<th style="width:120px">
	      						<select name="type">
	          						<option value="orderSn" >订单编号</option>
	          						<option value="refundSn" >退款编号</option>
	          						<option value="buyerName" >买家会员名</option>
	        					</select>：
	        				</th>
	        				<td class="w160"><input type="text" class="text" name="key" value="" /></td>
	        				<th>退款时间：</th>
	        				<td class="w260">
        							<input name="startTime"  type="text" class="txt Wdate" value="${startTime}" onClick="WdatePicker({dateFmt:'yyyy-MM-dd'});" />&#8211;
          							<input name="endTime" type="text" class="txt Wdate" value="${endTime}" onClick="WdatePicker({dateFmt:'yyyy-MM-dd'});" />
         					</td>
	        				<td class="w90 tc">
	        					<input type="submit" class="submit" value="搜索" />
	        				</td>
	      				</tr>
	    			</table>
	  			</form>
	  			<table class="ncu-table-style">
				    <thead>
				      	<tr>
				        	<th class="w180">订单编号</th>
				        	<th class="w180">退款编号</th>
				       	 	<th class="w80">买家会员名</th>
				        	<th class="w80">退款金额</th>
				        	<th class="w150">退款时间</th>
				        	<th class="w90">状态</th>
				        	<th class="w90">操作</th>
				      	</tr>
				    </thead>
				    <tbody>
				        <#if list??>
    							<#list list as refundLog>
		                  			<tr class="bd-line" >
		        						<td><span style="color: blue">${refundLog.orderSn}</span></td>
		        						<td class="goods-num">${refundLog.refundSn}</td>
		        						<td>
		        							<span style="color: blue">${refundLog.buyerName}</span>
		        						</td>
		        						<td>
			        						<em class="goods-price" title="${refundLog.refundPaymentName}">
			        							<script type="text/javascript">
					              					var refund = number_format(${refundLog.orderRefund},2);
					              					document.write(refund);
					              				</script>
			        						</em>
			        					</td>
		        						<td class="goods-time">${refundLog.createTimeStr?string('yyyy-MM-dd HH:mm:ss')}</td>
		        						<td>
		        							<#if refundLog.refundState==1>
		        								待审核
		        							<#elseif refundLog.refundState==2>
		        								审核通过
		        							<#elseif refundLog.refundState==3>
		        								已拒绝
		        							</#if>
		        						</td>
		        						<td>
		        							<a href="javascript:void(0)" onclick="refundLogDetail('${refundLog.logId}')"> 查看 </a>
		        							<#if refundLog.refundState==1>
		        								<a href="javascript:void(0)" onclick="refund('${refundLog.logId}')">审核</a>
		        							</#if>
		        						</td>
		      						</tr>
		      					</#list>
      						</#if>
				    </tbody>
	    			<tfoot>
					      <tr>
					        <td colspan="20">
					        	<#import "/trade/page.ftl" as q><!--引入分页-->
				                <#if recordCount??>
				                    <@q.pager pageNo=pageNo pageSize=pageSize recordCount=recordCount toURL="${toUrl}"/>
				                </#if>
					        </td>
					      </tr>
	    			</tfoot>
	      		</table>
			</div>
    	</div>
  	</div>
    <div class="clear"></div>
</div>
<script type="text/javascript">
	//查看退款信息
	function refundLogDetail(id){
	   	layer.open({
		    type: 2,
		    area: ['400px', '270px'],
		    skin: 'layui-layer-rim',
		    title: '退款详情',
		    content :  ['${base}/trade/refundLogDetail?logId=' + id, 'no']
		});
	}
	
	/**退款审核**/
	function refund(id) {
	   	layer.open({
		    type: 2,
		    area: ['500px', '400px'],
		    skin: 'layui-layer-rim',
		    title: '退款审核',
		    //content: APP_BASE + '/cart/addresslist'
		    content :  ['${base}/trade/refundOrderIndex?logId=' + id, 'no'],
		    success: function(layero, index){
		    	layer.getChildFrame('#confirm_button',index).on('click', function(){
					var logId=layer.getChildFrame("input[name='logId']",index).val();
					var refundState = layer.getChildFrame("input[name='refund_state']:checked",index).val();
					var refundMessage = layer.getChildFrame("textarea[name='refund_message']",index).val();
					var fmUrl = '${base}/trade/refundOrder';
					$.ajax({
			             type: "post",
			             url: fmUrl,
			             data: {logId:logId,refundState:refundState,refundMessage:refundMessage},
			             dataType: "json",
						 async:false,
						 success:function(data) {
							if(data.success){
								parent.layer.msg("审核成功!",{icon:1},function(){
									location.reload();
								});	
							}else{
								parent.layer.msg("审核失败!",{icon:2},function(){
									location.reload();
								});		
							}
						}
			         });  
				});	
		    }
		});
	}
</script>
</body>
</html>
<@p.footer/>	