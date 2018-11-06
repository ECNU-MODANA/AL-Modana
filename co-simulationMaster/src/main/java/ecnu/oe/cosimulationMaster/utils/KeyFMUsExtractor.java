package ecnu.oe.cosimulationMaster.utils;



import ecnu.oe.cosimulationMaster.model.FMU;

import java.util.HashMap;
import java.util.Iterator;

import java.util.List;
import java.util.Map;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2018/5/17.
 */
public class KeyFMUsExtractor {
    private static HashMap<String, int[]> sets;
    public static HashMap<String, int[]> extracts(List<FMU> fmuList, HashMap<String,String> dataExchange_map){

        StringBuilder errorFMUString = new StringBuilder("");
        StringBuilder dependFMUString = new StringBuilder("");
        StringBuilder restFMUString = new StringBuilder("");
        sets = new HashMap<>();
        if(fmuList == null)
            return sets;
        for(int i= 0;i<fmuList.size();i++){
            FMU fmu = fmuList.get(i);
            if(fmu.getType() == FMU.FMUType.de) {
                errorFMUString.append(i + ",");
                Iterator entries = dataExchange_map.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry) entries.next();
                    String key = (String)entry.getKey();
                    String value = (String)entry.getValue();
                    if(key.contains(fmu.getFMUName())) {
                        String output = value.substring(0, value.indexOf("."));
                        if (!dependFMUString.toString().contains(output))
                            dependFMUString.append(output + ",");
                    }
                }
            }
        }
        for(int j=0;j<fmuList.size();j++){
            if(errorFMUString.toString().contains(Integer.toString(j)) || dependFMUString.toString().contains(Integer.toString(j)))
                continue;
            restFMUString.append(Integer.toString(j)+",");
        }
        errorFMUString.substring(0,errorFMUString.length());

        String[] errFMU = errorFMUString.length() > 0 ? errorFMUString.toString().split(",") : null;
        String[] dependFMU = dependFMUString.length()>0 ? dependFMUString.toString().split(",") : null;
        String[] restFMU = restFMUString.length()>0 ? restFMUString.toString().split(",") : null;
        int[] errQue = errFMU == null ? new int[0] :new int[errFMU.length];
        int[] dependQue = dependFMU == null ? new int[0] : new int[dependFMU.length];
        int[] restQue = restFMU == null ? new int[0] : new int[restFMU.length];
        if (errFMU != null)
            for(int i=0;i<errFMU.length;i++)
                errQue[i] = Integer.parseInt(errFMU[i]);
        if (dependFMU != null)
            for(int i=0;i<dependFMU.length;i++)
                dependQue[i] = Integer.parseInt(dependFMU[i]);
        if (restFMU != null)
            for(int i=0;i<restFMU.length;i++)
                restQue[i] = Integer.parseInt(restFMU[i]);
        sets.put("error", errQue);
        sets.put("depend", dependQue);
        sets.put("rest", restQue);
        return sets;
    }

}
