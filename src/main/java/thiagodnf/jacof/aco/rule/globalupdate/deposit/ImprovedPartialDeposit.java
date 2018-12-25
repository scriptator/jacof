package thiagodnf.jacof.aco.rule.globalupdate.deposit;

import thiagodnf.jacof.aco.ACO;
import thiagodnf.jacof.aco.ant.Ant;
import thiagodnf.jacof.aco.subset.AbstractSubSet;

public class ImprovedPartialDeposit extends PartialDeposit{
    private int resetValue;
    double[][] deposits;
    public ImprovedPartialDeposit(ACO aco, double rate, AbstractSubSet subSet) {
        super(aco, rate, subSet);
        this.deposits=new double[aco.getProblem().getNumberOfNodes()] [aco.getProblem().getNumberOfNodes()];
        resetValue=aco.getProblem().getNumberOfNodes();

    }
    private void resetPheromones() {
        for(int i=0; i<deposits.length;i++) {
            for(int j=0;j<deposits[i].length;j++) {
                deposits[i][j]=0;
            }
        }
        calcDeposits();
    }
    public double getTheNewValue(int i, int j) {
        if(i<resetValue) resetPheromones();
        resetValue=i;
        return aco.getGraph().getTau(i, j) + rate * deposits[i][j];

    }
    private void calcDeposits() {
        int nNodes=aco.getProblem().getNumberOfNodes();
        for(Ant a: subSet.getSubSet()) {
            for(int k=0;k<nNodes;k++) {
                int i=a.getSolution()[k];
                int j=a.getSolution()[(k+1)%nNodes];
                double deltaTau=aco.getProblem().getDeltaTau(a.getTourLength(), i, j);
                if(a.path[i][j]==1) {
                    deposits[i][j]+= deltaTau;
                }
                if(a.path[j][i]==1) {
                    deposits[j][i]+= deltaTau;
                }
            }
        }
    }
}