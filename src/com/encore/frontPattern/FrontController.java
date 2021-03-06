package com.encore.frontPattern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.plaf.synth.SynthSpinnerUI;

import com.encore.jsonparser.PathJsonObject;
import com.encore.model.AddressVO;
import com.encore.model.UserVO;
import com.encore.place.BusPlace;
import com.encore.place.RouteSummaries;
import com.encore.place.SubwayPlace;
import com.encore.place.Movement;
import com.encore.service.ChromeDriverUtil;
import com.encore.util.DateUtil;
import com.encore.util.MapToJson;

/**
 * Servlet implementation class FrontController
 */
@WebServlet("*.go")
public class FrontController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String requestURI = request.getRequestURI().substring(request.getContextPath().length(),
				request.getRequestURI().length() - 3);
		System.out.println("requestURI: " + requestURI);
		response.setCharacterEncoding("utf-8");

		String signFolderName = "";
		HttpSession sess = request.getSession();

		if (requestURI.equals("/user/signOut")) {
			sess.invalidate();
			response.sendRedirect("../index.jsp");
			return;
		}

		CommonCtroller controller = null;
		Map<String, Object> map = new HashMap<>();
		String page = null;
		String method = request.getMethod().toLowerCase();
		map.put("method", method);
		switch (requestURI) {
		// 1. sign
		case "/user/sign":
			if (sess.getAttribute("user") != null)
				response.sendRedirect("../index.jsp");
			controller = new LoginController();
			if (method.equals("get")) {
			} else {
				map.put("id", request.getParameter("id"));
				map.put("password", request.getParameter("password"));
			}
			break;

		// 2. id 以묐났泥댄겕 , pw �솗�씤
		case "/user/IdCheckForm":
			controller = new IdCheckController();
			if (method.equals("get")) {
				map.put("id", (String) request.getParameter("id"));
			} else {
				// System.out.println("sess.user == null? " + (sess.getAttribute("user") ==
				// null));
				map.put("id", ((UserVO) sess.getAttribute("user")).getId());
				map.put("password", (String) request.getParameter("password"));
			}
			break;

		// 3.userInsert -- post �쉶�썝媛��엯, get �쉶�썝�젙蹂� �닔�젙
		case "/user/signUp":
			map.put("user",
					new UserVO(request.getParameter("id"), request.getParameter("password"),
							request.getParameter("name"), request.getParameter("email"), request.getParameter("gender"),
							DateUtil.stringToDate(request.getParameter("birthday")), request.getParameter("partner")));
			if (method.equals("get")) {
				// �쉶�썝 �젙蹂� �닔�젙
				controller = new UserUpdateController();
				page = signFolderName + "userResult.jsp";
			} else {
				// �쉶�썝 媛��엯
				controller = new UserInsertController();
				page = signFolderName + "userResult.jsp";
			}
			break;

		// 4.userDelete --�쉶�썝�깉�눜 泥섎━
		case "/user/userDelete":
			controller = new UserDeleteController();
			String userid = ((UserVO) sess.getAttribute("user")).getId();
			map.put("userid", userid);
			page = signFolderName + "userResult.jsp";
			break;

		// 5. addressInsert -- 二쇱냼 �엯�젰泥섎━
		case "/user/addressInsert":
			controller = new AddressInsertController();
			if (method.equals("get")) {
				// System.out.println("get �룄李�...");
				page = signFolderName + "addressInsert.jsp";
			} else {
				// System.out.println("post �룄李�...");
				List<AddressVO> addlist = new ArrayList<>();
				int index = 1;
				while (request.getParameter("addNo" + index) != null)
					addlist.add(new AddressVO(((UserVO) sess.getAttribute("user")).getId(), index,
							request.getParameter("addNo" + index++), "F"));
				map.put("addlist", addlist);
				// System.out.println(map.get("useradd"));
				page = signFolderName + "userResult.jsp";
			}
			break;

		case "/user/setMyAdd":
			controller = new setMyAddController();
			if (method.equals("get")) {
				map.put("id", ((UserVO) sess.getAttribute("user")).getId());
				page = signFolderName + "addlist.jsp";
			} else {
				map.put("id", ((UserVO) sess.getAttribute("user")).getId());
				map.put("addNo", request.getParameter("isMain"));
				page = signFolderName + "userResult.jsp";
			}
			break;
			
		case "/geo":
			System.out.println("geo.go 전송완료");
			controller = new GetPathInfoController();

			Map<String, List<Movement>> subwayMap = new HashMap<>();
			Map<String, List<Movement>> busMap = new HashMap<>();
			Map<String, List<Movement>> busAndSubwayMap = new HashMap<>();
			Map<String, List<RouteSummaries>> routeSummariesMap = new HashMap<>();

			try {
				String str = ChromeDriverUtil.daumWithJsoup2(request.getParameter("start").split(","),
						request.getParameter("arrive").split(","));

				String json = str.substring(str.indexOf('{'), str.length() - 1);

				// 李몄“寃쎈줈濡� 媛��졇���꽌 媛믪뿉 諛섏쁺.
				// 李몄“寃쎈줈濡� 媛��졇���꽌 媛믪뿉 諛섏쁺.
				PathJsonObject.subwayAndBusJsonByMap(json, busAndSubwayMap, routeSummariesMap);
				PathJsonObject.subwayJsonByMap(json, subwayMap, routeSummariesMap);
				PathJsonObject.busJsonByMap(json, busMap, routeSummariesMap);
				
				
				String routeSummariesJson = MapToJson.RoutesSummariesMapToJson(routeSummariesMap);
				String busJson = MapToJson.BusMapToJson(busMap);
				String subwayJson = MapToJson.SubwayMapToJson(subwayMap);
				String busAndSubwayJson = MapToJson.BusAndSubwayMapToJSon(busAndSubwayMap);
				
				response.getWriter().println(routeSummariesJson+"/"+subwayJson+"/"+busJson+"/"+busAndSubwayJson);

//				System.out.println(json);
				
//				System.out.println("busAndSubwayMap");	
//				System.out.println(busAndSubwayMap);
//				System.out.println("subwayMap");
//				System.out.println(subwayMap);
//				System.out.println("busMap");
//				System.out.println(busMap);
				
//				System.out.println();
				
				System.out.println("busJson");
				System.out.println(busJson);
				System.out.println("subwayJson");
				System.out.println(subwayJson);
				System.out.println("busAndSubwayJson");	
				System.out.println(busAndSubwayJson);
				
				return;
				/*
				 * 踰꾩뒪�� 吏��븯泥� �샎�빀寃쎈줈�뒗 �씪�떒 �젣�쇅. String busAndSubwayJson =
				 * MapToJson.busAndSubwayJson(busAndSubwayMap);
				 */

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;

		default:
			break;
		}
		controller.execute(map);

		if (requestURI.equals("/user/IdCheckForm") && method.equals("get")) {
			response.getWriter().println(map.get("message"));
			return;
		} else if (requestURI.equals("/user/IdCheckForm") && method.equals("post")) {
			response.getWriter().println(map.get("count"));
			if ((boolean) map.get("count") == true) {
				return;
			} else {
				return;
			}
		} else if (requestURI.equals("/user/userDelete")) {
			if ((int) map.get("userResult") > 0) {
				sess.invalidate();
				map.put("userResult", "�쉶�썝 �깉�눜媛� �꽦怨듭쟻�쑝濡� �씠猷⑥뼱議뚯뒿�땲�떎.");
			} else
				map.put("userResult", "�쉶�썝 �깉�눜媛� �씠猷⑥뼱吏�吏� �븡�븯�뒿�땲�떎.");
			page = signFolderName + "userResult.jsp";
		} else if (requestURI.equals("/user/signUp") && method.equals("get")) {
//			System.out.println("set session" + map.get("user"));
			sess.setAttribute("user", map.get("user"));
		}

		// 濡쒓렇�씤 �씤利앸릺硫� index.jsp濡� �씠�룞, �씤利앸릺吏� �븡�쑝硫� sign.jsp濡� �씠�룞 --> redirect �빐�빞�븿
		Object result = map.get("loginResult");
		if (result != null) {
			if (((String) result).equals("yes")) {
				sess.setAttribute("user", map.get("user"));
				response.getWriter().println("true");
				return;
			} else {
				response.getWriter().println("false");
				return;
			}
		}

		for (String key : map.keySet())
			request.setAttribute(key, map.get(key));

		RequestDispatcher view = request.getRequestDispatcher(page);
		view.forward(request, response);
	}

	private void alert() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FrontController() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
