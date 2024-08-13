package equalscale.SampleEvaluation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;


public class BatchSampleEvalution {
	
	/**
	 * read XES convert to XLog
	 * @param xesUri
	 * @return XLog
	 * @throws URISyntaxException
	 */
	public static XLog readAllFile(java.net.URI xesUri) throws URISyntaxException {
		List<XLog> xlogs = null;
        XesXmlParser xesParser = new XesXmlParser();
        
        File xesFile = new java.io.File(xesUri);
        boolean canParse = xesParser.canParse(xesFile);
        if (canParse)
        {   
			try {
				xlogs = xesParser.parse(xesFile);
			} catch (Exception e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
        }
        return xlogs.get(0);
	}
	//���ָ��·���µ�·����ַ
	public static LinkedList<File> filesPath1 = new LinkedList<File>();
	public static LinkedList<File> filesPath2 = new LinkedList<File>();
	/**
	 * ����ָ��·���µ������ļ�������ŵ�filesPath
	 * @param dir
	 */
	private static void addFilesPath1(String dir) {
		File file = new File(dir);
		if(file.exists()){
			if(file.isDirectory()){
				File[] listFiles = file.listFiles();
				for(int i = 0 ; i < listFiles.length ; i++ ){
					addFilesPath1(listFiles[i].getAbsolutePath());
				}
			}else{
				filesPath1.add(file);
			}
		}
	}
	private static void addFilesPath2(String dir) {
		File file = new File(dir);
		if(file.exists()){
			if(file.isDirectory()){
				File[] listFiles = file.listFiles();
				for(int i = 0 ; i < listFiles.length ; i++ ){
					addFilesPath2(listFiles[i].getAbsolutePath());
				}
			}else{
				filesPath2.add(file);
			}
		}
	}

	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
		System.out.println("11111111111111");	
		//���ԭʼ��־  һ��
		addFilesPath1("E:\\2020���й���\\����\\1.����LogRank++\\new\\sigrank\\111Origin");
		//��Ų�����־
		addFilesPath2("E:\\2020���й���\\����\\1.����LogRank++\\new\\sigrank\\ETMC4200");
		//step2:��ÿ���ļ����д���
		PrintWriter pw = new PrintWriter(new File("E:/20220216NewData/0314Result/0523ETMC4200Evalution.csv"));
		int count=0;
//		for(int i = 0 ;i < filesPath1.size();i++){
			System.out.println("************************************");	
			java.net.URI xesUri = new java.net.URI(filesPath1.get(0).toURI().toString());
			//step2-0:��ȡ�ļ���,����������ɲ�����־����
			String logName=filesPath1.get(0).toURI().toString();
//			System.out.println("logName=" +logName);
			String[] strArr = logName.split("/");
			String strArr1=strArr[strArr.length-1].split("\\.")[0];
			System.out.println("ԭʼ��־��:"+strArr1);
//			String str11=strArr1.split("_")[0]+"_"+strArr1.split("_")[1]+"_"+strArr1.split("_")[2];
//			System.out.println("ԭʼ��־��111:"+str11);
			XLog originalLog=readAllFile(xesUri);
			for(int j=0;j <filesPath2.size();j++) {
				System.out.println("///////////////////////////////////");	
				//�����ж�
				java.net.URI xesUri1 = new java.net.URI(filesPath2.get(j).toURI().toString());
				//step2-0:��ȡ�ļ���,����������ɲ�����־����
				String logName1=filesPath2.get(j).toURI().toString();
				String[] strArr2 = logName1.split("/");
//				String strArr3=strArr2[strArr2.length-1].split("\\.")[0]+"."+strArr2[strArr2.length-1].split("\\.")[1];
				String strArr3=strArr2[strArr2.length-1].split("\\.xes")[0];
				System.out.println("������־��:"+strArr3);
				String str=strArr3.split("_")[2];
				System.out.println("str:"+str);
				int sampleRatio=Integer.valueOf(str);
				XLog sampleLog=readAllFile(xesUri1);
				System.out.println("(double)(sampleRatio/100):"+(double)sampleRatio/100);
				//���ú���
				QualityMetrics returnValue=SimRankSamplingTechnique(originalLog, sampleLog,(double)sampleRatio/100);
				
//					System.out.println("returnValue:"+returnValue[0]);
				System.out.println("returnValue.getCoverage():"+returnValue.getCoverage());
				System.out.println("returnValue.getNAME():"+returnValue.getNAME());
				System.out.println("returnValue.getSMAPE():"+returnValue.getSMAPE());
//					returnValue.getNAME();
//					returnValue.getSMAPE();
				
				StringBuilder sb = new StringBuilder();
				sb.append(strArr1);
				sb.append(',');
				sb.append(strArr3);
				sb.append(',');
				sb.append(returnValue.getCoverage());
				sb.append(',');
				sb.append(returnValue.getNAME());
				sb.append(',');
				sb.append(returnValue.getSMAPE());
				sb.append('\n');
				pw.write(sb.toString());
				
				count++;
				System.out.println("��"+count+"�����У�����");
//				}	
//			}
		}
		pw.close();
		System.out.println("Ending!!!");	
	}
	public static QualityMetrics SimRankSamplingTechnique(XLog OriginalLog, XLog SampleLog,double SampleRatio)
	{
		//����˼��:
		//��һ�����õ�ֱ�Ӹ�����ϵ
		//����ָ�꣺coverage:������ ��ֱ�Ӹ�������/�¼���־�е�ֱ�Ӹ�����ϵ��������������Ϊ
		//����ָ��2������ȵ���Ԥ�ڵĲ�����Ϊ��ʵ�ʵĲ�����Ϊ֮������
		//           ������ eΪԤ�ڵ���Ϊ��sΪ��������Ϊ
		//			  ��һ��ƽ���������(NAME):����Ҫ�þ���ֱ�����б�����Ϳ��Ա�ʾ
		//			  �Գ�ƽ�����԰ٷֱ����(sMAPE):��Ϊ��Ƿ�������ܵ������صĳͷ�
		/*
		 * �㷨���裺
		 * 1.�����־�еı���
		 * �ҵ�ԭʼ��־�Ͳ�����־��ֱ�Ӹ�����ϵ��������־���Լ���������hashmap���� ������
		 * 2.e����Ƶ�Σ�
		 * 
		 * 
		 */	
		//���������裬����ΪSampleRatio,Ĭ��Ϊ30%
//		double SampleRatio=0.3;
		
		QualityMetrics qualityMetrics = new QualityMetrics();
//		HashMap<XTrace,Integer> traceFrequency_orginalLog=GetVariantLog(OriginalLog);
//		HashMap<XTrace,Integer> traceFrequency_sampleLog=GetVariantLog(SampleLog);
		
		
		HashMap<String,Integer> DfrNumber_original=getDfrNumber(OriginalLog);
		HashMap<String,Integer> DfrNumber_sample=getDfrNumber(SampleLog);
		
		double coverage=(double)DfrNumber_sample.size()/DfrNumber_original.size();
		System.out.println("DfrNumber_original.size(): " +DfrNumber_original.size());
		System.out.println("DfrNumber_sample.size(): " +DfrNumber_sample.size());
		System.out.println("coverage(������) = " +coverage);
		qualityMetrics.setCoverage(coverage);
		
	
		HashMap<String,Double> DfrNumber_except_original=new HashMap<>();
		Map<String, Integer> map0 = DfrNumber_original;
		for(Map.Entry<String, Integer> entry : map0.entrySet()){
//			System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
			DfrNumber_except_original.put(entry.getKey(), entry.getValue()*SampleRatio);
		}
		
		
		
		
		//1.����NAME
//		System.out.println("ԭʼ��־Ϊ:\n");
		Map<String, Double> map1 = DfrNumber_except_original;
		double fenzi=0;
		double fenmu=0;
		for(Map.Entry<String, Double> entry : map1.entrySet()){
//			System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
			fenmu+= entry.getValue();
			if(DfrNumber_sample.containsKey(entry.getKey())) {
//				System.out.println("DfrNumber_sample.get(entry.getKey()):"+DfrNumber_sample.get(entry.getKey()));
				fenzi+=Math.abs(entry.getValue()-(double)DfrNumber_sample.get(entry.getKey()));
			}else {
//				System.out.println("entry.getValue():"+entry.getValue());
				fenzi+=entry.getValue();
			}
		}

		
		System.out.println("fenzi=" +fenzi+ ", fenmu = " + fenmu);
		double NAME_value=fenzi/fenmu;
		System.out.println("NAME_value(ָ��1) = " + NAME_value);
		qualityMetrics.setNAME(NAME_value);
		
		
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
		System.out.println("sMAPE_value(ָ��2) = " + sMAPE_value);
		qualityMetrics.setSMAPE(sMAPE_value);
		
		return qualityMetrics;
	}
	/**
	 * ��ñ�����־
	 * @param OriginalLog
	 * @return
	 */
	public static HashMap<XTrace,Integer> GetVariantLog(XLog OriginalLog) {
		HashMap<XTrace,String> compareString=new HashMap<>();
		HashSet<String> compareString1=new HashSet<>();
		for (XTrace trace: OriginalLog)	{   ///all trace size!=0
			String mergeTransition="";
			for(XEvent event: trace){  
				String transition = event.getAttributes().get("concept:name").toString();       //Resource
				mergeTransition=mergeTransition+transition;
			}
			compareString1.add(mergeTransition);
			compareString.put(trace, mergeTransition);
		}
        //step1:�Թ켣Ƶ�����м���
		HashMap<XTrace,Integer> traceFrequency=new HashMap<>();
		for(XTrace trace1:OriginalLog)
		{
			int count=0;
			for(XTrace trace2:OriginalLog)
			{
				if(compareString.get(trace1).equals(compareString.get(trace2))) {
					count++;
				}
			}
			traceFrequency.put(trace1, count);
		}
		//step2-0:��õ�һ���� variant
		for(XTrace trace:OriginalLog) {
			String m1=compareString.get(trace);
			if(compareString1.contains(m1)) {//hashset
				compareString1.remove(m1);
			}else {
				traceFrequency.remove(trace);//ɾ���켣��ͬ�Ĺ켣
			}
		}
		return traceFrequency;
	}
	/**
	 * ������־��ֱ�Ӹ����Լ�Ƶ��
	 * @param OriginalLog
	 * @return
	 */
	public static HashMap<String,Integer> getDfrNumber(XLog OriginalLog) {
		//trace to direct-follow relation set
		HashMap<String, HashSet<String>> traceIDToDFRSet = new HashMap<>();
		for(XTrace trace:OriginalLog)
		{
			HashSet<String> dfrSet = new HashSet<>();
			for(int i =0;i<trace.size()-1;i++)
			{
				//add activity
//						activitySet.add(XConceptExtension.instance().extractName(trace.get(i)));
				//add directly follow pair
				dfrSet.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
			}
			traceIDToDFRSet.put(XConceptExtension.instance().extractName(trace), dfrSet);
		}
		
		//direct-follow relation set of the log
		HashSet<String> dfrSetLog = new HashSet<>();
		for(String traceID: traceIDToDFRSet.keySet())
		{
			dfrSetLog.addAll(traceIDToDFRSet.get(traceID));
		}
		//direct-follow relation to significance. the number of traces contain the drf divided by the total number of traces. 
		HashMap<String, Integer> dfrNumber = new HashMap<>();
		for(String dfr: dfrSetLog)
		{
			//count the number of traces that contains dfr
			int count =0;
			for(XTrace trace: OriginalLog)
			{
				for(int i =0;i<trace.size()-1;i++)
				{
					if(dfr.equals(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1))))
					{
						count++;
						break;
					}
				}
			}
			dfrNumber.put(dfr, count);
		}
		return dfrNumber;
	}
	
	/**
	 * ����켣��ֱ�Ӹ����Լ�Ƶ��
	 * @param OriginalLog
	 * @return
	 */
	public static HashSet<String> getDfrNumber(XTrace trace) {
		//trace to direct-follow relation set
		HashMap<String, HashSet<String>> traceIDToDFRSet = new HashMap<>();
		HashSet<String> dfrSet = new HashSet<>();
		for(int i =0;i<trace.size()-1;i++)
		{
			//add activity
//			activitySet.add(XConceptExtension.instance().extractName(trace.get(i)));
			//add directly follow pair
			dfrSet.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
		}
        return dfrSet;
	}
}
