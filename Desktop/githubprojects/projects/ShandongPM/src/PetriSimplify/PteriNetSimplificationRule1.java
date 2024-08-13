package PetriSimplify;

import java.util.Collection;

import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * ����1��ǰ�������ж�����ߣ�ֻ��һ���������Ӿ�Ĭ��Ǩ����Ĭ��Ǩ�ĺ�̿����ж����ߺͳ���
 * ��ʽ����ǰ����������߼ӵ���̿��������
 * @author ZSP
 *
 */
public class PteriNetSimplificationRule1 {
	public static boolean reduce(Petrinet net, Canceller canceller) {
		/*
		 * Iterate over all transitions.
		 */
		for (Transition transitionA : net.getTransitions()) {
			if (canceller.isCancelled()) {
				return true;
			}
			/*
			 * Check whether the transition is silent.
			 */
			if (!transitionA.isInvisible()) {
				continue;
			}

			/*
			 * Check input arc.
			 */
			Place placeX;
			{
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> arcs1 = net
						.getInEdges(transitionA);
				if (arcs1.size() != 1) {//���㾲Ĭ��Ǩ�����ֻ��һ��
					continue;
				}
				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc1 = arcs1.iterator().next();
				if (!(arc1 instanceof Arc)) {
					continue;
				}
				if(net.getInEdges(transitionA).size() !=1) {
					continue;
				};
				placeX = (Place) arc1.getSource();
//				System.out.println("net.getTransitions():"+net.getTransitions());
//				System.out.println("arcs1:"+arcs1);
//				System.out.println("placeX:"+placeX);
			}

			Place placeY = getPlaceY(net, transitionA);
			System.out.println("placeY:"+placeY);
			if (placeY == null) {
				continue;
			}

			/*
			 * Check that we're not dealing with a self loop
			 */
			if (placeX == placeY) {
				continue;
			}

			if(net.getOutEdges(placeX).size() !=1) {//ǰ�������ĳ���ֻ��һ��
				continue;
			}
			if(net.getInEdges(placeY).size() ==1) {//��̿�������߲�ֹһ��
				continue;
			}
			/*
			 * Relocate all input arcs from place X to place Y
			 * ɾ�����о�Ĭ��Ǩǰ����������ߣ�����ӵ���Ĭ��Ǩ��̿�����
			 */
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(placeX)) {
				Transition source = (Transition) edge.getSource();
				System.out.println("����X����ߵı�Ǩ:"+source);
//				int weight = ((Arc) edge).getWeight();
				net.removeEdge(edge);
//				net.addArc(source, placeY, weight);
				net.addArc(source, placeY);
			}
			
			//ϸ�ڲ��֣������Ĭ��Ǩû���ˣ���ô�������ı�Ҳ��û���ˣ�����������д���
			/*
			 * Relocate all output arcs from place X to place Y
			 * �Ƴ���Ĭ��Ǩǰ������֮��ı�
			 */
//			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(placeX)) {
//				net.removeEdge(edge);
////				net.addArc(placeY, target, weight);
//			}

			//remove the place
			net.removePlace(placeX);//�Ƴ�ǰ������
			net.removeTransition(transitionA);//�Ƴ���Ĭ��Ǩ

			return true;//��һ����Ĭ��Ǩ����������������do  whileѭ�������Բ�Ӱ��
		}

		return false;
	}

	public static Place getPlaceY(Petrinet net, Transition transitionA) {
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = net.getOutEdges(transitionA);
		if (postset.size() != 1) {//���㾲Ĭ��Ǩ�ĳ���ֻ��һ��
			return null;
		}
		PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc2 = postset.iterator().next();
		if (!(arc2 instanceof Arc)) {
			return null;
		}

//		if (((Arc) arc2).getWeight() != 1) {
//			return null;
//		}

		return (Place) arc2.getTarget();
	}

	public static Transition findTransitionB(Petrinet net, Place placeX, Place placeY) {
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc4 : net.getInEdges(placeX)) {

			if (((Arc) arc4).getWeight() == 1) {
				Transition transitionB = (Transition) arc4.getSource();
				
				if (!transitionB.isInvisible()) {
					continue;
				}

				/*
				 * transition B may only have one incoming arc; from place Y
				 */
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> arcs3 = net
						.getInEdges(transitionB);
				if (arcs3.size() != 1) {
					continue;
				}

				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc3 = arcs3.iterator().next();

				if (arc3.getSource() != placeY) {
					continue;
				}

				if (((Arc) arc3).getWeight() != 1) {
					continue;
				}

				return transitionB;
			}
		}
		return null;
	}
}
