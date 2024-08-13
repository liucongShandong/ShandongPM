
package equalscale.SampleMethods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import equalscale.SampleEvaluation.SampleEvalutionPlugin;
@Plugin(
		name = "1111111--(Quickly Sampling)A Novel Euqal Scale Event Log Sampling",// plugin name
		returnLabels = {"Sample Log"}, //return labels
		returnTypes = {XLog.class},//return class
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Large Event Log"},
		userAccessible = true,
		help = "This plugin aims to sample an input large-scale example log and returns a small sample log by measuring the significance of traces." 
		)
public class QuicklyAlgorithm {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl"
	        )
	@PluginVariant(
			variantLabel = "Sampling Big Event Log, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public static XLog DijkstraPlusSampling(UIPluginContext context, XLog originalLog)
	{
		
		double startTime_total=0;
		double endTime_total=0;
		//set the sampling ratio
//		double samplingRatio = ProMUIHelper.queryForDouble(context, "Select the sampling ratios", 0, 1,	0.3);		
//		context.log("Interface Sampling Ratio is: "+samplingRatio, MessageLevel.NORMAL);	
		startTime_total=System.currentTimeMillis();
		XLog log=DijkstraPlus(originalLog);
		endTime_total=System.currentTimeMillis();

		System.out.println("***********************");
		System.out.println("DijkstraPlus Time:"+(endTime_total-startTime_total));
		System.out.println("***********************");
		context.log("DijkstraPlus Time:"+(endTime_total-startTime_total));	
		return log;
	}
	
	//������־�����������־
	public static XLog DijkstraPlus(XLog originalLog) {
		
		XLog sampleLog = new XLogImpl(originalLog.getAttributes());
		double currentCost=0.0;//��ǰ����
		HashMap<XTrace,Boolean> StatusVisited=new HashMap<>();//����״̬
		HashMap<XTrace,Double> traceWeight=new HashMap<>();//Ȩ��ϵ��
		
		
		XLog newOriginalLog = new XLogImpl(originalLog.getAttributes());
		newOriginalLog=(XLog) originalLog.clone();//����Ŀ�¡
		
		//����dfr������ֵ
		HashMap<String,Integer> DfrNumber_original=SampleEvalutionPlugin.getDfrNumber(originalLog);
		//���������裬����ΪSampleRatio,Ĭ��Ϊ30%
		double SampleRatio=0.3;
		HashMap<String,Double> DfrNumber_except_original=new HashMap<>();
		Map<String, Integer> map0 = DfrNumber_original;
		for(Map.Entry<String, Integer> entry : map0.entrySet()){
			DfrNumber_except_original.put(entry.getKey(), entry.getValue()*SampleRatio);
//			System.out.println("�켣���� " + entry.getKey()+"ֵ�� " + entry.getValue()*SampleRatio);
		}
		
		//Ԥ����MAX
		
		for(XTrace trace:originalLog) {
			int count=0;
			Map<String, Double> map00 = sortDescend(DfrNumber_except_original);
			String firstall=null;
			
			for(Entry<String, Double> entry : map00.entrySet()){
				firstall=entry.getKey();
				System.out.println("1111�켣���� " + entry.getKey()+";ֵ�� " + entry.getValue());
			}
		}
		Map<String, Double> map00 = sortDescend(DfrNumber_except_original);
		String firstall=null;
		
		for(Entry<String, Double> entry : map00.entrySet()){
			firstall=entry.getKey();
			System.out.println("�켣���� " + entry.getKey()+";ֵ�� " + entry.getValue());
			break;
		}
		//���´���  ��������ֱ�Ӹ�����ϵ�Ĺ켣�����뵽������־��
		//��֤��������̫��
		double Allcount=originalLog.size()*0.2;
		double count00=0.0;
		HashMap<XTrace,Boolean> StatusVisited000=new HashMap<>();//����״̬
		for(XTrace trace :originalLog){
			HashSet<String> DfrNumber_trace=SampleEvalutionPlugin.getDfrNumber(trace);
//			System.out.println("1111111 "+trace.getAttributes().get("concept:name").toString()+";;;firstall:"+firstall);
			if(DfrNumber_trace.contains(firstall) ) {
				System.out.println("1111111 ");
				sampleLog.add(trace);
				newOriginalLog.remove(trace);
				count00++;
			}
			if(count00 >Allcount) break;
		}
		//Ԥ����    �ӵ������Ǻ��ʵ�
		
		
		
		
		//1.��ʼ����ϵ�������켣Ȩ�ص�һ�γ�ʼ��traceWeight,���켣״̬������Ϊδ����
		for (XTrace trace: newOriginalLog)	{   ///all trace size!=0
			XLog sampleLog1 = new XLogImpl(originalLog.getAttributes());
			sampleLog1=(XLog) sampleLog.clone();//����Ŀ�¡
			sampleLog1.add(trace);
			currentCost=CurrentCost(DfrNumber_except_original,sampleLog1);//��ǰԽСԽ��
			StatusVisited.put(trace, false);//��ʼ��ȫ���Ĺ켣��Ϊδ����״̬
			traceWeight.put(trace, currentCost);
		}		
		//2.ѡ����С��mseֵ��Ϊ��ʼ��
		Map<XTrace, Double> map3 = sortAscend(traceWeight);
		int count=0;
		XTrace FirstTrace=null;
		for(Entry<XTrace, Double> entry : map3.entrySet()){
//			System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
//			if(count ==0) {
				sampleLog.add(entry.getKey());//����Сֵ��ӵ�������־��
				FirstTrace=entry.getKey();
				System.out.println("��ѡ��ĵ�һ���켣 Ϊ= " + entry.getKey().getAttributes().get("concept:name").toString());
//				System.out.println("NAMPEֵΪ�� " + CurrentCost(DfrNumber_except_original,sampleLog));		
				break;
//			}
		}
////		 XTrace minKey = getMapMinOrMaxValueKey(traceWeight, "min");
//		//////////////////////////////////////
//		
//		//3.��ʼ���켣��ǰ׺���PreTrace  ��¼�켣ǰ����key���켣��value:key��ǰ��
//		HashMap<XTrace,XTrace> PreTrace=new HashMap<>();
//		for (XTrace trace: originalLog)	{
//			PreTrace.put(trace, FirstTrace);//��ʼ���켣ǰ��
//		}
//		
		//˼·������
		
		//trace_pearson���� key���켣��value��weightֵ
		//status key:trace  value: boolean
		//������һ��weight hashmap  key:trace value:name(double)
		//path:��¼ǰ���� trace   trace
		//�ж�������weight.get(trace0).value + currentcost(tarce1) <= weight.get(trace1)
		
		
		//4.����
		//���ʶ�Ĺ켣Ϊ��һ���켣
		XTrace trace00=FirstTrace;
//		double mse=0.0;
//		int countValue=1;
		double PrecurrentCost=currentCost;//��ǰ����
		boolean flag=true;
		
		
//		Map<XTrace, Double> textmap3 = traceWeight;
		
		
		while(!isAllVisited(StatusVisited) && flag) {//��ǰ������״̬�������ʵ����������ֵС��һ����ֵ(0.01)����ֹѭ��	
//		while(flag) {//��ǰ������״̬�������ʵ����������ֵС��һ����ֵ(0.01)����ֹѭ��	
			// ������ڵ�����Ϊ�ѷ���
			StatusVisited.put(trace00, true);
            // �鿴�ڽӾ�������ָ���ڵ��ڽӵĽڵ�
//	     	int value=0;
            for (XTrace trace:newOriginalLog) {
//            	value++;
            	//��ǰ�켣����������־��ʱ���д���
//            	 System.out.println("value= " + value);
//            	if(!sampleLog.contains(trace)) {
            	if(!StatusVisited.get(trace)) {
                    // ���ܵ���·��Ȩֵ: ���ʼ��ָ����㵽������㵽�ýڵ��·��Ȩֵ�ܺ�
                    double newWeight;//�켣����Ȩ��
                    // ����ڵ�δ����, �����ڽӽڵ�  д����
//                  newWeight=traceWeight.get(trace00)+CurrentCost(originalLog,sampleLog,trace00,trace);
                    newWeight=CurrentCost(DfrNumber_except_original,sampleLog,trace);
                    
                    //�����Ȩ��С��ԭ����Ȩ�ض��ҽڵ�δ�����ʹ�
                    //traceWeight.get(trace):ԭ����Ȩ��
                    //newWeight:���������켣���Ȩ��
//                    System.out.println("newOriginalLog= " + originalLog.size());
//                    System.out.println("��ѡ��Ĺ켣 Ϊ= " + trace.getAttributes().get("concept:name").toString());
//                    System.out.println("newWeight:"+newWeight);
//                    System.out.println("traceWeight.get(trace):"+traceWeight.get(trace));
                    if(newWeight<traceWeight.get(trace)) {//������������㣿
                    	// ����¸ýڵ����С·��ֵ, ���¸ýڵ��ǰ��Ϊ�������
                    	traceWeight.put(trace,newWeight);   //����Ȩ��
                    	
                    	
//                    	PreTrace.put(trace, trace00);   //����ǰ׺�ڵ�
                    	sampleLog.add(trace);//����������־
                    	StatusVisited.put(trace, true);
//                    	originalLog.remove(trace);//�������켣�Ƴ�
                    	//��ѡ��Ĺ켣Ϊ
                    	System.out.println("11111��ѡ��Ĺ켣 Ϊ= " +trace.getAttributes().get("concept:name").toString());
                    }
            	}
            }
          //1.ѡ����һ����ֵ��С�Ľڵ���δ�����ʹ���,��ʱ�켣Ȩ����Ҫ����
    		Map<XTrace, Double> map4 = sortAscend(traceWeight);
    		int count1=0;
    		for(Entry<XTrace, Double> entry : map4.entrySet()){
    			if(count1 ==0 && !StatusVisited.get(entry.getKey()) ) {
    				sampleLog.add(entry.getKey());//����������־
    				trace00=entry.getKey();
    				originalLog.remove(entry.getKey());//�������켣�Ƴ�
    				System.out.println("*******************************");
    				System.out.println("��ѡ��Ĺ켣 Ϊ= " + entry.getKey().getAttributes().get("concept:name").toString());
//    				System.out.println("NAMPEֵΪ�� " + CurrentCost(DfrNumber_except_original,sampleLog));		
    				break;
    			}
    		}
    		double allCurrentCost=CurrentCost(DfrNumber_except_original,sampleLog);
    		
    		System.out.println("��ǰֵ allCurrentCost:" +allCurrentCost);
    		System.out.println("��ǰֵ PrecurrentCost:" +PrecurrentCost);
//    		if(PrecurrentCost > allCurrentCost && allCurrentCost >0.1) {
    		if( PrecurrentCost > allCurrentCost) {//����ֲ����Ž�   �ж������ǲ���Ҫ��һ�£����ǵ����ݼ���
    			PrecurrentCost=allCurrentCost;
    			System.out.println("111��ǰֵ���ڵ�ǰֵ ����" );	
//    		}else if(allCurrentCost> 0.3){
//    			PrecurrentCost=allCurrentCost;
//    			System.out.println("222��ǰֵ���ڵ�ǰֵ ����" );	
//    		}
    		}else {
    			flag=false;
    			System.out.println("����nameֵΪ:" +allCurrentCost);
    			System.out.println("��ǰֵС�ڵ�ǰֵ ������" );	
    		}
    		
    		
            // �������from����Ϊ: weights��������ֵ��С�Ĳ���δ���ʵĽڵ�
    		//����Ȩ��
    		for (XTrace trace: newOriginalLog)	{   ///all trace size!=0
    			if(!StatusVisited.containsKey(trace)) {
	    			XLog sampleLog3 = new XLogImpl(originalLog.getAttributes());
	    			sampleLog3=(XLog) sampleLog.clone();//����Ŀ�¡
	    			sampleLog3.add(trace);
	    			currentCost=CurrentCost(DfrNumber_except_original,sampleLog3);//��ǰԽСԽ��
	    			traceWeight.put(trace, currentCost);
    			}
    		}
//    		countValue++;
		}
		
		return sampleLog;
	}
	
	/**
     * ����Ƿ�ȫ��������(ֻҪ��һ����δ����������false)
     *
     * @return boolean
     */
    public static boolean isAllVisited(HashMap<XTrace,Boolean> StatusVisited) {
    	Map<XTrace, Boolean> map4 =StatusVisited ;
		for(Entry<XTrace, Boolean> entry : map4.entrySet()){
			if(!entry.getValue()) {
				return false;
			}
		}
        return true;
    }
	/**
	 * 
	 * @param originalLog
	 * @param sampleLog
	 * @return ��ǰ���۵�ֵ
	 */
	public static double CurrentCost(HashMap<String,Double> DfrNumber_except_original,XLog sampleLog) {
		HashMap<String,Integer> DfrNumber_sample=SampleEvalutionPlugin.getDfrNumber(sampleLog);
		//2.�Գ�ƽ����� sMAPE
		Map<String, Double> map2 = DfrNumber_except_original;

		double abs_sum=0;
		for(Map.Entry<String, Double> entry : map2.entrySet()){
			double fenzi1=0;
			double fenmu1=0;
			if(DfrNumber_sample.containsKey(entry.getKey())) {
				fenzi1+=Math.abs(entry.getValue()-(double)DfrNumber_sample.get(entry.getKey()));
				fenmu1+=entry.getValue()+(double)DfrNumber_sample.get(entry.getKey());
				abs_sum+=fenzi1/fenmu1;
			}else {
				abs_sum+=1;
			}
		}
		double sMAPE_value=abs_sum/ DfrNumber_except_original.size();
//		System.out.println("sMAPE_value(ָ��2) = " + sMAPE_value);
		return sMAPE_value;
	}
	
	
	/**
	 * 
	 * @param originalLog
	 * @param sampleLog
	 * @return ��ǰ���۵�ֵ
	 */
	public static double CurrentCost(HashMap<String,Double> DfrNumber_except_original,XLog sampleLog,XTrace trace) {
		XLog sampleLog2 = new XLogImpl(sampleLog.getAttributes());
		sampleLog2=(XLog) sampleLog.clone();//����Ŀ�¡
		sampleLog2.add(trace);
		HashMap<String,Integer> DfrNumber_sample=SampleEvalutionPlugin.getDfrNumber(sampleLog2);


		//2.�Գ�ƽ����� sMAPE
		Map<String, Double> map2 = DfrNumber_except_original;

		double abs_sum=0;
		for(Map.Entry<String, Double> entry : map2.entrySet()){
//			fenmu+= entry.getValue();
			double fenzi1=0;
			double fenmu1=0;
			if(DfrNumber_sample.containsKey(entry.getKey())) {
				fenzi1+=Math.abs(entry.getValue()-(double)DfrNumber_sample.get(entry.getKey()));
				fenmu1+=entry.getValue()+(double)DfrNumber_sample.get(entry.getKey());
				abs_sum+=fenzi1/fenmu1;
			}else {
				abs_sum+=1;
			}
		}
		double sMAPE_value=abs_sum/ DfrNumber_except_original.size();
//		System.out.println("sMAPE_value(ָ��2) = " + sMAPE_value);
		return sMAPE_value;
	}
	
	/************************************************�����㷨***************************************/
    // Map��valueֵ��������
    public static <K, V extends Comparable<? super V>> Map<K, V> sortDescend(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                int compare = (o1.getValue()).compareTo(o2.getValue());
                return -compare;
            }
        });
 
        Map<K, V> returnMap = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            returnMap.put(entry.getKey(), entry.getValue());
        }
        return returnMap;
    }
 
    // Map��valueֵ��������
    public static <K, V extends Comparable<? super V>> Map<K, V> sortAscend(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                int compare = (o1.getValue()).compareTo(o2.getValue());
                return compare;
            }
        });
 
        Map<K, V> returnMap = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            returnMap.put(entry.getKey(), entry.getValue());
        }
        return returnMap;
    }
}
