package CrossOrgMining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

//import extract.OrganizationConfig;
/**
 * this class aims to discover the cross-organization interaction relation among activities
 * @author cliu3
 *
 */
public class CrossOrganizationInteractionRelationDiscovery {

	// discover interaction relation among activities that belong to different organizations. 
	//�������ڲ�ͬ��֯�Ļ֮��Ľ�����ϵ��
	public static HashSet<CrossOrganizationInteraction> discoverCrossOrganizationInteractions(OrganizationConfig orgConfig, XLog originalLog)
	{
//		System.out.println("33333333333333333333333333");
		HashSet<CrossOrganizationInteraction> allInteractions = new HashSet<>();
		HashMap<String, List<String>> AllmessageSent=new HashMap<String, List<String>>();
			 //AllmessageSent=null;
		HashMap<String, List<String>> AllmessageRec=new HashMap<String, List<String>>();
		HashMap<String, List<String>> PublicRes=new HashMap<String, List<String>>();
			 
		HashMap<String, List<String>> TOrg=new HashMap<String, List<String>>();
				
		boolean flag=true;
			//get the activity set to sub log mapping 
			//�������Ϊ����־ӳ��
		HashMap<HashSet<String>, XLog> ActivitySet2OrgLog = new HashMap<>();
		
		for(XTrace trace: originalLog) {
			for(XEvent event: trace) {
				String resource = event.getAttributes().get("resource").toString();       //Resource
				String messageSent = event.getAttributes().get("Message:Sent").toString();   //��ȡ���͵���Ϣ
				String messageRec = event.getAttributes().get("Message:Rec").toString();    ///��ȡ���յ���Ϣ
				String transition = event.getAttributes().get("concept:name").toString();       //Resource
				String organization = XOrganizationalExtension.instance().extractResource(event);   //Org
				String[] spiltMessageSent=messageSent.split(",");
				String[] spiltMessageRec=messageRec.split(",");
				String[] spiltOrg=organization.split(",");
				String[] spiltRes=resource.split(",");
				
//petri.addPlace(messageRec);
				List<String> putList=new ArrayList<>();
				List<String> putList1=new ArrayList<>();
				for(int k=0;k<spiltMessageSent.length;k++) {
					String mes=spiltMessageSent[k].trim();
//					System.out.println("��"+k+"��mes:"+mes);
					//������Ϣ��ʵ��
					if (!mes.equals("null")) { //������ȡ����Ϊ����Ϣֵ    ////������ȡ��������Ϣ�ı�Ǩ	
						if(AllmessageSent.isEmpty()) {//��һ����Ǩ��
							putList.add(mes);
							AllmessageSent.put(transition, putList);
							}
						else {
								if(!AllmessageSent.containsKey(transition) || spiltMessageSent.length>1) {//��һ������
									putList.add(mes);
									AllmessageSent.put(transition, putList);
								}
							}
//								 System.out.println("AllmessageSent������:"+AllmessageSent);
					 }
				 }
				for(int m=0;m<spiltMessageRec.length;m++) {
					String msr=spiltMessageRec[m].trim();
					//�յ���Ϣ��ʵ��
					if (!msr.equals("null")){         //������ȡ����Ϊ����Ϣֵ    ////������ȡ���յ���Ϣ�ı�Ǩ 
						if(AllmessageRec.isEmpty()) {
							putList1.add(msr);
							AllmessageRec.put(transition, putList1);
							}else {
								if(!AllmessageRec.containsKey(transition)|| spiltMessageRec.length>1) {
									putList1.add(msr);
									AllmessageRec.put(transition, putList1);
								}
							}
//							System.out.println("AllmessageRec������:"+AllmessageRec);
					 }
				 }
				/*-----------------------------------------��Դ--------------------*/
				List<String> putRes=new ArrayList<>();
				/////////////////////////////////////////////////////// 
				for(int k=0;k<spiltRes.length;k++) {
					String res = spiltRes[k].trim();
					//��Դ��ʵ��
					if (!res.equals("null")) {         //������ȡ����Ϊ����Դֵ    ////��Դ
						if(PublicRes.isEmpty()) {//��һ����Ǩ���
							putRes.add(res);
							PublicRes.put(transition, putRes);
							}else {
								if(!PublicRes.containsKey(transition) || spiltRes.length>1) {//��һ������
									putRes.add(res);
									PublicRes.put(transition, putRes);
								}
						    }
					  }
				 }
				}//trace
			}//log

//		System.out.println("AllmessageSent������:"+AllmessageSent);
//		System.out.println("****************************************");
//		System.out.println("AllmessageRec������:"+AllmessageRec);
		
		Set set = AllmessageSent.keySet();    //���з�����Ϣ�ı�Ǩ
		Set set0 = AllmessageRec.keySet();   //���н�����Ϣ�ı�Ǩ
		Set setres1 = PublicRes.keySet();   //��Դ�ı�Ǩ
		Set setres2 = PublicRes.keySet();   //��Դ�ı�Ǩ

/*----------��Դ����----------------------------------------------------------------------------------------------*/
		for(Iterator iterRes1 = setres1.iterator(); iterRes1.hasNext();){
			String transition1 = (String)iterRes1.next();
			List<String> value1 = PublicRes.get(transition1);    
				    
			for(Iterator iterRes2 = setres2.iterator(); iterRes2.hasNext();){
				String transition2 = (String)iterRes2.next();
				List<String> value2 = ( List<String>)PublicRes.get(transition2); 
				
				if(value1.equals(value2)&& !(transition1.equals(transition2))) {
					String sourceActivityOrg = orgConfig.getOrganization4Activity(transition1);
					String targetActivityOrg = orgConfig.getOrganization4Activity(transition2);
					if(sourceActivityOrg!=null && targetActivityOrg!=null && !sourceActivityOrg.equals(targetActivityOrg)){
						//create an interaction
						OrgActivity sourceOrgActivity = new OrgActivity(transition1, sourceActivityOrg);
						OrgActivity targetOrgActivity = new OrgActivity(transition2, targetActivityOrg);
						////�ĵ�
//						OrgResource sourceOrgResource = new OrgResource(transition1, sourceActivityOrg, value1.toString());
//						OrgResource targetOrgResource = new OrgResource(transition2, targetActivityOrg, value1.toString());
								
						HashSet<OrgActivity> sources = new HashSet<>();
						sources.add(sourceOrgActivity);
						HashSet<OrgActivity> targets = new HashSet<>();
						targets.add(targetOrgActivity);
							
						CrossOrganizationInteraction inter= new CrossOrganizationInteraction(sources, targets,value1.toString());
						allInteractions.add(inter);
					  }
				}	
			}
		}

		String regEx ="[\n`~!@#$%^&*()+=|{}':;'\\[\\].<>/?~��@#��%����&*()����+|{}�������������������� ����]";
    	String aa = " ";
    	String regExSp = ",";
        Pattern psp = Pattern.compile(regExSp);
        Pattern p = Pattern.compile(regEx);
        
		 /*--------------------------------------------------------------------------------------------------------*/
		for(Iterator iter = set.iterator(); iter.hasNext();) {
			String transition = (String)iter.next();
			
			
////		System.out.println(m.find()); 
//			if(m.equals(true)) {
//				System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"); 
//			}	
			List<String> value = AllmessageSent.get(transition);    ///AllmessageSent : transition==>value(message)
			
//			System.out.println("Value:"+value);
			for(Iterator iterRec = set0.iterator(); iterRec.hasNext();){
				String transitionRec = (String)iterRec.next();
				List<String> valueRec = ( List<String>)AllmessageRec.get(transitionRec);   ///AllmessageRec : transition==>value(message)
				
				if((value.size()==1)) {
					if((valueRec.size()==1)) {
						if(value.equals(valueRec)) {
							//get the source and target of each relation
							//��ȡÿ����ϵ����Դ��Ŀ��
							String sourceActivityOrg = orgConfig.getOrganization4Activity(transition);
							String targetActivityOrg = orgConfig.getOrganization4Activity(transitionRec);
							
							//check the belonging organization of them, if they are different, then create an interaction.
							//������ǵ�������֯,������ǲ�ͬ,�򴴽�һ�������� 
							if(sourceActivityOrg!=null && targetActivityOrg!=null
									&& !sourceActivityOrg.equals(targetActivityOrg)){
								//create an interaction
								OrgActivity sourceOrgActivity = new OrgActivity(transition, sourceActivityOrg);
								OrgActivity targetOrgActivity = new OrgActivity(transitionRec, targetActivityOrg);
									
								HashSet<OrgActivity> sources = new HashSet<>();
								sources.add(sourceOrgActivity);
								HashSet<OrgActivity> targets = new HashSet<>();
								targets.add(targetOrgActivity);
										
								CrossOrganizationInteraction inter= new CrossOrganizationInteraction(sources, targets,value.toString());
								allInteractions.add(inter);
										
//								System.out.println("inter:"+inter.getSourceActivities());
//								System.out.println("inter:"+inter.getTargetActivities());
//								System.out.println("**************************************");
							  }
							}///if(value.equals(valueRec))
					}
					else 
						for(int i =0; i<valueRec.size() ; i++) {
//							System.out.println("value:"+value.get(0));
//							System.out.println("valueRec.get(i):"+valueRec.get(i));
							if(value.get(0).equals(valueRec.get(i))) {
//								System.out.println("***************************");
								//get the source and target of each relation
								//��ȡÿ����ϵ����Դ��Ŀ��
								String sourceActivityOrg = orgConfig.getOrganization4Activity(transition);
								String targetActivityOrg = orgConfig.getOrganization4Activity(transitionRec);

//								System.out.println("transition:"+transition);
//								System.out.println("transitionRec:"+transitionRec);
								
								//check the belonging organization of them, if they are different, then create an interaction.
								//������ǵ�������֯,������ǲ�ͬ,�򴴽�һ�������� 
								if(sourceActivityOrg!=null && targetActivityOrg!=null
										&& !sourceActivityOrg.equals(targetActivityOrg)){
									//create an interaction
									OrgActivity sourceOrgActivity = new OrgActivity(transition, sourceActivityOrg);
									OrgActivity targetOrgActivity = new OrgActivity(transitionRec, targetActivityOrg);
										
									HashSet<OrgActivity> sources = new HashSet<>();
									sources.add(sourceOrgActivity);
									HashSet<OrgActivity> targets = new HashSet<>();
									targets.add(targetOrgActivity);
											
									CrossOrganizationInteraction inter= new CrossOrganizationInteraction(sources, targets,value.toString());
									allInteractions.add(inter);
											
//									System.out.println("inter:"+inter.getSourceActivities());
//									System.out.println("inter:"+inter.getTargetActivities());
//									System.out.println("**************************************");
								  }
								}///if(value.equals(valueRec))
						}
				}///if((value.size()==1)) 
				else
					for(int i =0; i<value.size() ; i++) {
//						System.out.println("value:"+value.get(0));
//						System.out.println("valueRec.get(i):"+value.get(i));
						if((valueRec.size()==1)) {
							if(valueRec.get(0).equals(value.get(i))) {
								//get the source and target of each relation
								//��ȡÿ����ϵ����Դ��Ŀ��
								String sourceActivityOrg = orgConfig.getOrganization4Activity(transition);
								String targetActivityOrg = orgConfig.getOrganization4Activity(transitionRec);
								
								//check the belonging organization of them, if they are different, then create an interaction.
								//������ǵ�������֯,������ǲ�ͬ,�򴴽�һ�������� 
								if(sourceActivityOrg!=null && targetActivityOrg!=null
										&& !sourceActivityOrg.equals(targetActivityOrg)){
									//create an interaction
									OrgActivity sourceOrgActivity = new OrgActivity(transition, sourceActivityOrg);
									OrgActivity targetOrgActivity = new OrgActivity(transitionRec, targetActivityOrg);
										
									HashSet<OrgActivity> sources = new HashSet<>();
									sources.add(sourceOrgActivity);
									HashSet<OrgActivity> targets = new HashSet<>();
									targets.add(targetOrgActivity);
											
									CrossOrganizationInteraction inter= new CrossOrganizationInteraction(sources, targets,valueRec.toString());
									allInteractions.add(inter);
											
//									System.out.println("inter:"+inter.getSourceActivities());
//									System.out.println("inter:"+inter.getTargetActivities());
//									System.out.println("**************************************");
								  }
								}///if(value.equals(valueRec))
						}
						
					}
					
					


				
			    }
			}
	
		return allInteractions;
		
	}
	
}
