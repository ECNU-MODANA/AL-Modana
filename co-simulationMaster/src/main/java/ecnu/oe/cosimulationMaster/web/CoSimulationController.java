package ecnu.oe.cosimulationMaster.web;

import com.google.gson.Gson;
import ecnu.oe.cosimulationMaster.ma.PSRMA;
import ecnu.oe.cosimulationMaster.model.FMU;
import ecnu.oe.cosimulationMaster.model.State;
import ecnu.oe.cosimulationMaster.model.Trace;
import ecnu.oe.cosimulationMaster.model.TraceBean;
import ecnu.oe.cosimulationMaster.utils.File2Redis;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;

import java.util.*;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2018/10/28.
 */
@RestController
public class CoSimulationController {
    private String filePath = "";
    @RequestMapping(value = "/psrma", method = RequestMethod.POST)
    public @ResponseBody String coSimulateWithPSRMA(@RequestBody Map<String, Object> params) throws Exception{
        System.out.println("I got it");
        ArrayList<FMU> fmuList = getFMUList((ArrayList<String>)params.get("fmuList"));
        //将redis中保存的fmu持久化到本地
        ShardedJedis jedis = File2Redis.pool.getResource();
        ArrayList<String> storedList = File2Redis.storeFMUtoLocal(fmuList,jedis);
        if(params !=null) {
            System.out.println(params.get("redisAddress"));
            System.out.println(Arrays.toString(storedList.toArray()));
        }
        LinkedHashMap<String, String> mappingMap = (LinkedHashMap<String, String>)params.get("ioMapping");
        Iterator iterator = mappingMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>)iterator.next();
            System.out.println(entry.getKey() + "----------"+entry.getValue());
        }
        HashMap<String, String> dataExchange_map = adjustIOMapping(mappingMap, storedList);
        PSRMA psrma = new PSRMA(false, false, false);
        Trace trace = psrma.simulateOne(fmuList, dataExchange_map, filePath, 3000 * 15, 15,
                    false, ',', null);
        return new Gson().toJson(trace);
    }


    public HashMap<String, String> adjustIOMapping(LinkedHashMap<String, String> mappingMap,ArrayList<String> storedList) {
        Map<String, Integer> addressNumMap = new HashMap<>();
        for (int i = 0;i<storedList.size();i++) {
            String address = storedList.get(i);
            addressNumMap.put(address.substring(address.lastIndexOf("/")+1,address.lastIndexOf(".")), i);
        }
        Iterator iterator = mappingMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>)iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            String newKey = addressNumMap.get(key.substring(0,key.indexOf(".")))+"."+key.substring(0,key.indexOf("."))+"." + key.substring(key.lastIndexOf(".")+1);
            String newValue = addressNumMap.get(value.substring(0,value.indexOf(".")))+"."+value.substring(0,value.indexOf("."))+"." + value.substring(value.lastIndexOf(".")+1);
            mappingMap.remove(entry.getKey());
            mappingMap.put(newKey, newValue);
        }
        return mappingMap;

    }

    public ArrayList<FMU> getFMUList(ArrayList<String> fileList){
        ArrayList<FMU> fmuList = new ArrayList<FMU>();
        filePath = "";
        for(int i=0;i<fileList.size();i++){
            FMU fmu = new FMU();
            String s = fileList.get(i);
            String prefix = System.getProperty("user.dir");
            filePath = filePath + prefix+s.substring(s.lastIndexOf("/")) +",";
            fmu.setFMUName(s.substring(s.lastIndexOf("/")+1,s.indexOf(".")));
            fmu.setFMUAddress(s);
            fmuList.add(fmu);
        }
        filePath = filePath.substring(0,filePath.length()-1);
        return fmuList;
    }
}
