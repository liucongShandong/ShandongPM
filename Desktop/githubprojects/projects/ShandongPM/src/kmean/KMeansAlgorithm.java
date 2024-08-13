package kmean;



/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
//import org.processmining.analysis.traceclustering.distance.DistanceMetric;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Minseok Song
 */
public class KMeansAlgorithm   {



	protected ArrayList<Integer> traceList;
	protected int clusterSize;//��ʼ�������СΪ4
	protected int traceSize;
	protected ArrayList<Double> currentDistanceSum;
	protected ArrayList<ClusterSet> clustersList;
	protected ArrayList<Integer> frequencyList;
	protected ClusterSet clusters;
	protected ClusterSet clustersforOthers;
	//protected DistanceMetric distanceMeasures;
	protected DoubleMatrix2D distanceMatrix;
	protected DoubleMatrix2D distances;
	protected static String ST_FREQUENCY = "Frequency";
	protected static String ST_DISTANCE = "Distance";
	protected ArrayList<Integer> TraceIdList1;
	protected ArrayList<String> traceIdList0;

	protected KMeansAlgorithm() {
		
		// TODO �Զ����ɵĹ��캯�����
	}
	/**
	 * logRank++��������������
	 * TraceIdList:����˵�洢��trace id
	 * nameToTrace���洢����trace  ���¼�
	 * matrix���洢����������
	 * double[][] matrix = new double[TraceIdList.size()][TraceIdList.size()];
	 * ��ŵ��ǵ�i���켣����j���켣�������ԣ�Ҳ����˵�Ǿ���
	 * ������distence���Ӧ
	 * @throws FileNotFoundException 
	 */
	
	public ClusterSet runKmean(XLog log,HashMap<String, XTrace> nameToTrace,ArrayList<String> TraceIdList,double[][] matrix,int clusterSize) 
	{
		ClusterSet clusters = new ClusterSet(log);
		//��N���ɹ켣�Ĵ�С
		/**
		 *1.��ʼ��
		 * (1).��ʼ�����ĵ�
		 * ����Ĵ�С����ͨ�������ã����Ļ��ٴ���
		 */	
		
	    // initialize clusters
		ClusterSet oldClusterSet = new ClusterSet(log);
		System.out.println("oldClusterSet:"+oldClusterSet.getClusters());
		//ӳ��켣��
	    HashMap<String, Integer> mapTraceName=new HashMap<String, Integer>();
	    for(int i=0;i<TraceIdList.size();i++) {
	    	mapTraceName.put(TraceIdList.get(i), i);
	    }
		//��ʼ������ ������˾�������ĵ����Էֱ�Ϊ0��1��2��3

		/**
		 * 2.��ʼ���࣬�������ֹ����Ϊ�ﵽ�����������Լ��¾�����ھɾ���
		 * iteration_number����ʾ������������Ĭ������Ϊ50
		 */
		/**
		 * (2).��ʼ������ʹ���༯�ϣ�����������Ϊ�յ���־
		 */
		for (int i = 0; i < clusterSize; i++) {
			XLog clusterLog = new XLogImpl(log.getAttributes());
			Cluster cluster = new Cluster(clusterLog, "Cluster " + i,i);
			clusters.addCluster(cluster);
		}
		int iteration_number=100;
		HashMap<Integer, Integer> center1= new HashMap<>();
//		ArrayList<Integer> list1=new ArrayList<Integer>();
		for (int k = 0; k < iteration_number; k++) {
			clusters.clear();//���һ�¾��༯��
			System.out.println("��"+(k+1)+"�ε�����");
			   //��Ӿ���
			   //��ʼ�� ��ΪCluster1 Cluster2 ...
	        //	calculate distance between each center point and each instance
			/**
			 * ��1��������룬����ֱ����ǰ�洫�����������Ծ���������
			 * int iteration_number=50;
			 * matrix1[i][j]��ʾ�켣i�͹켣j�ľ��룬ӦΪ�Գƾ���
			 * 	clusterSize:�����С������˵���������		 
			 * **/
			 for (int i = 0; i < clusterSize; i++) {
				XLog clusterLog = new XLogImpl(log.getAttributes());
				if(k==0) {
					Cluster cluster = new Cluster(clusterLog, "Cluster " + i,i);
					clusters.addCluster(cluster);
				}else {
					Cluster cluster = new Cluster(clusterLog, "Cluster " + i,center1.get(i));
					clusters.addCluster(cluster);
				}
				
			}
			/**
			 * (3).��ʼ��ÿ�����������
			 */
			System.out.println("��ʼ��clusters:"+clusters);
			for (int i = 0; i < clusterSize; i++) {
				System.out.println("��"+i+"��������Ϊ��"+clusters.getClusters().get(i).getName());
				System.out.println("��"+i+"�����������Ϊ��"+clusters.getClusters().get(i).getClusterCenter());
				//clusters.getClusters().get(i).getClusterCenter();
			}
			/** int array[][];
           		�����ȣ�������
               	int lenX = array[0].length;
          		����߶ȣ�������
              	int lenY = array.length;
			 */
			for (int i = 0; i < matrix.length; i++) {
				int index = -1;
				double value = Double.MAX_VALUE;
				for (int j = 0; j < clusterSize; j++) {
					if(i != clusters.getClusters().get(j).getClusterCenter()) {
						if (value > matrix[i][clusters.getClusters().get(j).getClusterCenter()]) {
							index = j;
							value = matrix[i][clusters.getClusters().get(j).getClusterCenter()];
						}
					}
				}
//				clusters.getClusters().get(index).setCurrentDistanceSum(clusters.getClusters().get(index).getCurrentDistanceSum()+value);
//				System.out.println("��"+i+"������");
				//�㷨����ÿ���켣�;������ĵ�Ƚϣ�ȡ��С���Ǹ�
				//System.out.println("��������"+clusters.getClusters());//Cluster0, Cluster1, Cluster2, Cluster3
//				System.out.println("clusters.getClusters().get(index):"+clusters.getClusters().get(index));
				
//				clusters.getClusters().get(index).addTrace(traceList.get(i));
				//TraceIdList
//				for(int k=0;k<TraceIdList.size();k++){
//					if((clusters.getClusters().get(i).getTraceIndices().get(j)).toString().equals(TraceIdList.get(k))) {
//						XTrace clusterTrace=nameToTrace.get(TraceIdList.get(k));
//						clusters.getClusters().get(i).getLog().add(clusterTrace);
//					}
//				}
				//����켣����Ϊ���ֵĻ������������
				//TraceIdList.get(i)������õ�trace0
				
				//clusters.getClusters().get(index).getLog().add(e);
				//�ҵ���Ӧ��ֵ
				clusters.getClusters().get(index).addTrace(mapTraceName.get(TraceIdList.get(i)));
				//clusters.getClusters().get(index).addTrace(Integer.parseInt(TraceIdList.get(i)));
//				System.out.println("getTraceIndices��"+clusters.getClusters().get(index).getTraceIndices());	
			}
//			System.out.println("999clusters:"+clusters);
			/**
			 * 4.�ظ���������
			 * ֱ��������ھ��࣬˵�����ĵ㲻�ٸı�
			 */
			// recalculate center
			center1.clear();
			center1=calculateCenter(clusters,matrix,clusterSize);
			try {
				if (k == 0) {
					for (int i = 0; i < clusterSize; i++)
					oldClusterSet = (ClusterSet) clusters.clone();
//					System.out.println("oldClusterSet:"+oldClusterSet.getClusters());
				} else {
					if (clusters.equals(oldClusterSet)) {
//						System.out.println("�˳��˳��˳�");
						break;
					} else {
						oldClusterSet = (ClusterSet) clusters.clone();
					}
				}
			} catch (Exception e) {}
			
	   }
		System.out.println("����� ����"+clusters);
		ArrayList<String>  centerName=new ArrayList<String>();
		//��ε���˼��˵�����켣��ӵ����ۺõ������־��
		for (int i = 0; i < clusterSize; i++) {
			//clusters.getClusters().get(i) �����ļ���
			System.out.println("��ʼ������==================================");
			int center11=clusters.getClusters().get(i).getClusterCenter();
			System.out.println("��"+i+"�����������Ϊ��"+clusters.getClusters().get(i).getClusterCenter());
			System.out.println("TraceIdList������Ϊ��"+TraceIdList.size());
//			System.out.println("dasdasdadsa��"+clusters.getClusters().get(i).getTraceIndices().get(center11));
			System.out.println("�������Ĺ켣����"+TraceIdList.get(center11));
			centerName.add(TraceIdList.get(center11));
//			System.out.println("���Ĺ켣����Ϊ��"+mapTraceName.get(TraceIdList.get(clusters.getClusters().get(i).clusterCenter)));
//			System.out.println("�����ֵΪ��"+clusters.getClusters().get(i).getTraceIndices());
			System.out.println("clusters.getClusters().get(i).getTraceIndices().size():"+clusters.getClusters().get(i).getTraceIndices().size());
			for(int j=0;j<clusters.getClusters().get(i).getTraceIndices().size();j++) {
//				System.out.println("j="+j);
//				System.out.println("clusters.getClusters().get(i).getTraceIndices().get(j)��["+clusters.getClusters().get(i).getTraceIndices().get(j)+"]");
				for(int k=0;k<TraceIdList.size();k++){
//					System.out.println("22["+mapTraceName.get(TraceIdList.get(k))+"]");
					if((clusters.getClusters().get(i).getTraceIndices().get(j)).equals(mapTraceName.get(TraceIdList.get(k)))) {
//						System.out.println("111111111111"+mapTraceName.get(TraceIdList.get(k)));
//						System.out.println("eeeee"+nameToTrace.get(TraceIdList.get(k)));
						XTrace clusterTrace=nameToTrace.get(TraceIdList.get(k));
						clusters.getClusters().get(i).getLog().add(clusterTrace);
						break;
					}
				}
				
			}
		}
		System.out.println("�������Ĺ켣����"+centerName);
		return clusters;
    }


	
	private HashMap<Integer, Integer> calculateCenter(ClusterSet clusters2,double[][] matrix,int clusterSize)
	{//���ĵļ�����ڸ�cluster�е����ĵ㵽����ľ����ƽ��ֵ
		/**
		 * 1.���Ȼ�ø��������ĵ�͸�������Ԫ�صĸ���i
		 * 2.���ƽ��ֵ������currentDistanceSum/i
		 * 3�����¾������ĵ�͵�����õ�ƽ��ֵ
		 */
		System.out.println("0000000");
//		ArrayList<Integer> list1=new ArrayList<Integer>();
		HashMap<Integer, Integer> center=new HashMap<Integer, Integer>();
		for(int i=0;i<clusterSize;i++) {
			int index=0;//��¼�±�
//			System.out.println("clusterSize:"+clusterSize);
			double value = Double.MAX_VALUE;;//��¼��С��ֵ
			for(int j=0;j<clusters2.getClusters().get(i).getTraceIndices().size();j++) {
				//�����ж���������Ļ�Ҳ����˵����Ҫ3����
//				System.out.println("2222222222200"+j+"  "+clusters2.getClusters().get(i).getTraceIndices().size());
				if(clusters2.getClusters().get(i).getTraceIndices().size() >2 ) {
					//����ÿ���㵽������ľ��룬����ľ���ȡΪ���ĵ�
					int points=clusters2.getClusters().get(i).getTraceIndices().get(j);
					double current_instance=0;
					for(int k=0;k<clusters2.getClusters().get(i).getTraceIndices().size();k++) {
						int otherPoint=clusters2.getClusters().get(i).getTraceIndices().get(k);
						current_instance+=matrix[points][otherPoint];
					}
					if (value > current_instance ) {//������վ������value�����¼�±��ֵ
						index = j;
						value = current_instance;
					}
				}else {
					break;
				}			
			}
			//�������ĵ�����
			if(clusters2.getClusters().get(i).getTraceIndices().size() >2) clusters2.getClusters().get(i).setClusterCenter(index);
			center.put(i,clusters2.getClusters().get(i).getClusterCenter());
//			list1.add(clusters2.getClusters().get(i).getClusterCenter());
//			System.out.println("nengbuneng���������Ϊ11111��"+clusters2.getClusters().get(i).getClusterCenter());
		}
		return center;
	}
}


