package java8.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.base.client.requests.Request;
import com.peersafe.base.client.responses.Response;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.chainsql.core.Chainsql;

public class PayStatistic {
	private static Chainsql c = Chainsql.c;
	public static String rootAddress = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
	public static String rootSecret = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";
	private static int m_sequence;
	private static Map<String,Integer> mapTime = new HashMap<String,Integer>();
	public static void main(String[] args) {
		if(args.length == 0) {
			System.out.println("请输入账户文件名参数");
			return;
		}
		c.connect("ws://192.168.0.108:6006");
		c.as(rootAddress, rootSecret);
		
		final List<String> listAddr = readFile(args[0]);
		prepareSeq(rootAddress);
		
		final long startTime = System.currentTimeMillis();    //获取开始时间
		for(int i=0; i<listAddr.size(); i++) {
			String[] arr = listAddr.get(i).split(",");
			JSONObject signed = signPay(arr[0],m_sequence++);
			Chainsql.event.subscribeTx(signed.getString("hash"), new Callback<JSONObject>() {
				@Override
				public void called(JSONObject data) {
					if(data.get("status").equals("validate_success")) {
						long endTime = System.currentTimeMillis();    //获取开始时间
						String hash = data.getJSONObject("transaction").getString("hash");
						mapTime.put(hash, Integer.valueOf((int)(endTime - startTime)));
						if(mapTime.size() == listAddr.size()) {
							print(mapTime);
						}
						System.out.println(mapTime.size());
					}else {
						System.out.println(data);
					}
				}
			});
//			System.out.println(i);
			Request req = c.connection.client.submit(signed.getString("tx_blob"), true);
			req.once(Request.OnError.class, new Request.OnError() {
				@Override
				public void called(Response response) {
					System.out.println("response:" + response.message.toString());
				}
			});
			req.request();
//			System.out.println(i);
//			if(i % 10 == 0) {
//				try {
//					Thread.sleep(50);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
		}
	}
	
	@SuppressWarnings("finally")
	public static List<String> readFile(String fileName){
		List<String> list = new ArrayList<String>();
		File file = new File(fileName);
        BufferedReader reader = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                list.add(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
            return list;
        }
	}
	
	private static void print(Map<String,Integer> map) {
		for(String key :map.keySet()) {
			System.out.println(key + "," + map.get(key));
		}
	}
	
	private static JSONObject signPay(String address,int sequence){
		JSONObject obj = new JSONObject();
		JSONObject tx_json = new JSONObject();
		tx_json.put("Account", rootAddress);
		tx_json.put("Amount", "1000000000");
		tx_json.put("Destination", address);
		tx_json.put("TransactionType", "Payment");
		tx_json.put("Sequence", sequence);
		obj.put("tx_json", tx_json);
		
		JSONObject res = c.sign(obj, rootSecret);
		return res;
	}
	private static void prepareSeq(String address) {
		//获取账户信息，得到当前Sequence
		AccountID account = AccountID.fromAddress(address);
		Request request = c.connection.client.accountInfo(account);
		if(request.response.result!=null){
			Integer sequence = (Integer)request.response.result.optJSONObject("account_data").get("Sequence");
			m_sequence = sequence.intValue();
		}
	}
}
