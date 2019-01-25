package com.koscom.burning.api;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koscom.burning.util.HttpClientUtil;

@Controller
public class RecommendStockApi {
	
	Logger logger = LoggerFactory.getLogger(RecommendStockApi.class);
	
    private static String URI_PREFIX = "http://localhost:8888/logpresso/httpexport/query.json";
	//private static String APIKEY = "847d4dcc-7284-bfa5-e447-b3c48cded77b";
	private static String APIKEY = "dd579650-45ca-4b3a-1fc4-57308be36625";
	
	Map<String, Object> map = new HashMap<String, Object>();

	@Autowired
	private HttpClientUtil httpClientUtil;
	
	ObjectMapper mapper = new ObjectMapper();
	
	@RequestMapping(value="/recommend")
	public @ResponseBody String getRecommendData(Model model, @RequestParam("id") String id) throws JsonParseException, JsonMappingException, IOException  {
		
		logger.info("api is called");
		
		String comName = null;
		String recName = null;
		String industryCode = null;
		String query = null;
		StringBuffer sb = new StringBuffer();
		
		
		// 1. 기업명
		query = "table market_join_simple | search 단축코드 == \"" + id + "\" | limit 1 | fields 기업명, 산업코드";
		map = this.callLogpresso(query);
		
		if(map == null || map.equals("")) {
			sb.append("해당 기업의 거래내역이 없습니다.");
			return sb.toString();
		}
		
		comName = map.get("기업명").toString();
		industryCode = map.get("산업코드").toString();
		
		sb.append(comName);
		sb.append("에 관심있는 투자자님!").append(System.getProperty( "line.separator"));
		
		
		// 2. 상승세/하락세 확인
		// 최근 10분간 거래 데이터의 등락율 평균이 양수이면 상승세로 판단, 음수이면 하락세로 판단
		//String jsonStr = "{\"sum\":10.75, \"평균\":-1.75}";
		query = "table marketdata_trade | search 단축코드 == \"" + id +
				"\" | sort -체결시간 | limit 10 | order 등락율 | stats sum(double(등락율)) as sum | eval avg = sum/10";
		
		
		map = this.callLogpresso(query);
		//int avg = Integer.parseInt(map.get("avg").toString());
		double avg = Double.parseDouble(map.get("avg").toString());
		
		if(avg > 0) {
		   sb.append("해당 기업은 지난 10분간 계속 상승세에 있습니다.").append(System.getProperty( "line.separator"));
		   recName = this.searchStock(id, "UP", industryCode);
		   String tmp = "다른 산업군에서 상승세에 있는 " + recName + " 의 주식은 어떠세요?";
		   sb.append(tmp);
		   
		} else {
			sb.append("해당 기업은 지난 10분간 계속 하락세에 있습니다.").append(System.getProperty( "line.separator"));
			recName = this.searchStock(id, "DOWN", industryCode);
			String tmp = "같은 산업군에서 상승세에 있는 " + recName + " 의 주식은 어떠세요?";
			sb.append(tmp);
		}
		
		
		return sb.toString();
	}


	private String searchStock(String id, String status, String industryCode) throws JsonParseException, JsonMappingException, IOException {
		
		String recName = null;
		String query = null;
		Map<String, Object> map = new HashMap<String, Object>(); // convert JSON string to 
		
		
		if(status.equals("UP")) {
			query = "table recommend | search 산업코드 != \"" + industryCode + "\"  | sort -updownavg | sort -tradesum | limit 1 ";
			map = this.callLogpresso(query);
			recName = map.get("기업명").toString();
		} else if(status.equals("DOWN")) {
			query = "table recommend | search 산업코드 == \"" + industryCode + "\"  and updownavg > 0 | sort -updownavg | limit 1 ";
			map = this.callLogpresso(query);
			recName = map.get("기업명").toString();
		}
		
		return recName;
	}
	
	private Map callLogpresso(String query) throws JsonParseException, JsonMappingException, IOException {
		
		
		String jsonStr = null;
		jsonStr = httpClientUtil.execute(URI_PREFIX + "?_apikey=" + URLEncoder.encode(APIKEY, "UTF-8") 
		+ "&_q=" + URLEncoder.encode(query, "UTF-8"));
		
		if(jsonStr != "" && !jsonStr.isEmpty() ) {
			map = mapper.readValue(jsonStr, new TypeReference<Map<String, String>>(){});
		} else {
			map = null;
		}
		
		
		return map;
	}
}
