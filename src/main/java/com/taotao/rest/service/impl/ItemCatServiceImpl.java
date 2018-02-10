package com.taotao.rest.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.ExceptionUtil;
import com.taotao.common.utils.JsonUtils;
import com.taotao.mapper.TbItemCatMapper;
import com.taotao.pojo.TbContent;
import com.taotao.pojo.TbItemCat;
import com.taotao.pojo.TbItemCatExample;
import com.taotao.pojo.TbItemCatExample.Criteria;
import com.taotao.rest.dao.JedisClient;
import com.taotao.rest.pojo.CatNode;
import com.taotao.rest.pojo.CatResult;
import com.taotao.rest.service.ItemCatService;

/**
 * 商品分类服务
 * <p>Title: ItemCatServiceImpl</p>
 * <p>Description: </p>
 * <p>Company: www.itcast.com</p> 
 * @author	入云龙
 * @date	2015年9月7日下午2:44:41
 * @version 1.0
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	@Autowired
	private JedisClient jedisClient;
	
	@Value("${INDEX_ITEM_CAT_REDIS_KEY}")
	private String INDEX_ITEM_CAT_REDIS_KEY;
	
	@Override
	public CatResult getItemCatList() {
		
		CatResult catResult = new CatResult();
		//查询分类列表,从redis中取值，如果没有则从数据库中取,并把结果放到redis里面
		try {
			//取出全部商品分类
			String contentCid ="0";
			String result = jedisClient.hget(INDEX_ITEM_CAT_REDIS_KEY, contentCid);
			System.out.println("jedisClient的INDEX_ITEM_CAT_REDIS_KEY取值成功");
			if (!StringUtils.isBlank(result)) {
				List<CatNode> resultList = JsonUtils.jsonToList(result, CatNode.class);
				catResult.setData(resultList);
				return catResult;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List resultList = new ArrayList<>();
		resultList = getCatList(0);
		catResult.setData(resultList);
		try {//把结果放到redis里面
			//把list转换成字符串
			String cacheString = JsonUtils.objectToJson(resultList);
			String contentCid ="0";
			jedisClient.hset(INDEX_ITEM_CAT_REDIS_KEY, contentCid, cacheString);
			System.out.println("jedisClient的INDEX_ITEM_CAT_REDIS_KEY设置成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return catResult;
	}
	
	/**
	 * 查询分类列表
	 * <p>Title: getCatList</p>
	 * <p>Description: </p>
	 * @param parentId
	 * @return
	 */
	private List<?> getCatList(long parentId) {
		//创建查询条件
		TbItemCatExample example = new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(parentId);
		//执行查询
		List<TbItemCat> list = itemCatMapper.selectByExample(example);
		//返回值list
		List resultList = new ArrayList<>();
		//向list中添加节点
		int count = 0;
		for (TbItemCat tbItemCat : list) {
			//判断是否为父节点
			if (tbItemCat.getIsParent()) {
				CatNode catNode = new CatNode();
				if (parentId == 0) {
					catNode.setName("<a href='/products/"+tbItemCat.getId()+".html'>"+tbItemCat.getName()+"</a>");
				} else {
					catNode.setName(tbItemCat.getName());
				}
				catNode.setUrl("/products/"+tbItemCat.getId()+".html");
				catNode.setItem(getCatList(tbItemCat.getId()));
				
				resultList.add(catNode);
				count ++;
				//第一层只取14条记录，原因是超过版面设计
//				if (parentId == 0 && count >=14) {
//					break;
//				}
			//如果是叶子节点
			} else {
				resultList.add("/products/"+tbItemCat.getId()+".html|" + tbItemCat.getName());
			}
		}
		
		return resultList;
	}

}
