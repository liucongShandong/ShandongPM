package equalscale.SampleEvaluation;
/*
 * this class defines the fitness, precision, generalization metrics. 
 */
public class QualityMetrics {

	private double Coverage =0.0;//������
	private double NAME =0.0;//��һ��ƽ���������
	private double SMAPE =0.0;//�Գ�ƽ�����԰ٷֱ����
	public double getNAME() {
		return NAME;
	}
	public void setNAME(double nAME) {
		NAME = nAME;
	}
	public double getSMAPE() {
		return SMAPE;
	}
	public void setSMAPE(double sMAPE) {
		SMAPE = sMAPE;
	}
	public double getCoverage() {
		return Coverage;
	}
	public void setCoverage(double coverage) {
		Coverage = coverage;
	}

	
}
