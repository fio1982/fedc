package models;

import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloMultiCriterionExpr;
import ilog.concert.IloNumExpr;
import ilog.cp.IloCP;
import objectives.EdgeServer;
import objectives.User;

public class EADModel {

	private int mbudget;
	private int[][] mDistanceMatrics;
	
	
	private double mAllUsers;
	private double mCost;
	private double mAllBenefits;
	private double mbenefitefficiency;
	private double mSquare;
	private double mAllBenefit_square;

	private int mServersNumber;
	private int mBudget;
	private double mFairness_index;
	private int[][] mAdjacencyMatrix;
	private int[][] mUserCovered; 
	private int[][] mUserBenefits;
	private List<User> mUsers;// All Users
	
	//2021
	//private double mfairnessindex;
	private double mfairnessdegree; //make the original value = 0
	private double mfairness_efficiency;
    private double mfairness_degree;
	//no need to add another variable for caching cost, we can use the 
	
	private List<Integer> mValidUserList; //Covered Users by selected servers
	
	private List<Integer> mSelectedServerList;
	private List<Integer> mBenefitList;
	//private List<Integer> mUserList;
	
	public EADModel(int serversNumber, double fairness_index, int[][] adjacencyMatrix, int[][] userCovered, int[][] userBenefits, List<User> users, 
			List<EdgeServer> servers) {
		mServersNumber = serversNumber;
		mFairness_index = fairness_index;
		mAdjacencyMatrix = adjacencyMatrix;
		
		//ÕâÀïÊÇbenefit
		//mUserCovered = userCovered;
		mUsers = users;
		
		//ÕâÀïÊÇbenefit
		mUserBenefits = userBenefits;
		mValidUserList = new ArrayList<>();
		mSelectedServerList = new ArrayList<>();
		
		//mUserList = new ArrayList<>();
		mBenefitList = new ArrayList<>();	
	}

	public void runnEAD() {
		try {
			//new cplex objective
			IloCP cp = new IloCP();
			
			//r ÊÇcaching±äÁ¿ 0/1
			IloIntVar[] r = cp.intVarArray(mServersNumber, 0, 1);
			
			//ÏßÐÔ±äÁ¿ ×îºóÓÅ»¯Ä¿±ê
			IloNumExpr eExpr = cp.linearNumExpr();
			
			//µÚ¶þ¸öÏßÐÔ±äÁ¿ È«²¿ÓÃ»§benefitµÄºÍ
			IloLinearNumExpr rExpr = cp.linearNumExpr();
			
			//µÚ¶þ¸öÏßÐÔ±äÁ¿ È«²¿ÓÃ»§benefitµÄºÍ
			IloNumExpr dExpr = cp.linearNumExpr();
			
			//µÚÈý¸öÏßÐÔ±äÁ¿ È«²¿ÓÃ»§benefitµÄÆ½·½µÄºÍ
			IloNumExpr sExpr = cp.linearNumExpr();
			
			//µÚËÄ¸öÏßÐÔ±äÁ¿ ´æ´¢r[]µÃºÍ È«²¿cost£¿
			IloNumExpr jainExpr = cp.linearNumExpr();
			
			IloNumExpr[] exprs = new IloNumExpr[2];
			
			//È«²¿±»coverµÄÓÃ»§µÄÊýÁ¿£¬³¤¶ÈÊÇuserÊýÁ¿£¬×îºóÇóºÍ
			IloNumExpr[] maxBenifitsExprs = new IloNumExpr[mUsers.size()];
			
			//ÕâÀïÉèÖÃÒ»¸öÒ»Î¬Êý×é£¬´æ´¢Ã¿¸öbenefitµÄÆ½·½
			IloNumExpr[] maxSquareBenifitsExprs = new IloNumExpr[mUsers.size()];
			
			//ÓÃ»§ºÍserverÖ®¼äcover¹ØÏµµÄ¶þÎ¬Êý×é
			IloNumExpr[][] userBenefitsExprs = new IloNumExpr[mUsers.size()][mServersNumber];

			//ÕâÀïÊÇ¼ÆËãÓÃ»§coverµÄ¶þÎ¬Êý×é ºÍcachingµÄÒ»Î¬Êý×é
			for (int i = 0; i < mServersNumber; i++) {
				rExpr.addTerm(1, r[i]);
				for (int j = 0; j < mUsers.size(); j++) {
					userBenefitsExprs[j][i] = cp.prod(mUserBenefits[i][j], r[i]);
				}
			}

			//ÕâÀï¼ÆËãÈ«²¿µÄbenefit Ò»Î¬Êý×é
			for (int j = 0; j < mUsers.size(); j++) {
				maxBenifitsExprs[j] = cp.max(userBenefitsExprs[j]);
			}

			//ÕâÀïÊÇÏßÐÔ±äÁ¿ È«²¿benefitµÄ×ÜºÍ
			dExpr = cp.sum(maxBenifitsExprs);
			
			//È«²¿benefit×ÜºÍµÄÆ½·½ ÓÅ»¯Ä¿±êµÄ·Ö×Ó
			dExpr = cp.square(dExpr);
			
			//ÕâÀï maxSquareÊý×é´æ´¢Ã¿¸öÓÃ»§benefitµÄÆ½·½
			for (int j = 0; j < mUsers.size(); j++) {
				maxSquareBenifitsExprs[j] = cp.prod(maxBenifitsExprs[j],maxBenifitsExprs[j]);
			}
			
			//ÕâÀïÊÇÃ¿¸öbenefitµÄÆ½·½µÄºÍ 
			sExpr = cp.sum(maxSquareBenifitsExprs);
			
			//³ËÒÔÓÃ»§ÊýÁ¿ ×÷Îª·ÖÄ¸
			sExpr = cp.prod(sExpr, mUsers.size());
			
			//ÓÅ»¯Ä¿±ê
			jainExpr = cp.quot(dExpr, sExpr);
			
			//jains index quot mcost
			eExpr = cp.quot(jainExpr, rExpr);

			//exprs[0] = rExpr;
			//exprs[1] = cp.negative(eExpr);

			//IloMultiCriterionExpr moExpr = cp.staticLex(exprs);
			//优化f
		//	cp.add(cp.maximize(jainExpr));
//08172021优化f/c
			cp.add(cp.maximize(eExpr));
			
			//cp.add(cp.maximize(eExpr));
			
			//IloConstraint u = cp.ge(r[0], 0); //ÖÁÉÙÑ¡ÔñÒ»¸öserver
			
			IloConstraint u = cp.ge(jainExpr, 0.75);
			for (int i = 0; i < mUsers.size(); i++) {
				u = cp.and(u, cp.ge(maxBenifitsExprs[i], 1)); //Ã¿¸öÓÃ»§µÄbenefit¶¼ÖÁÉÙÎª1
			}
		
			cp.add(u);
			
			//IloConstraint u = cp.ge(mbudget,rExpr);
			//IloConstraint u == for(int j = 0; j < mUsers.size(); j++) {cp.ge(maxBenifitsExprs[j],1);} //ÔõÃ´Ñù±íÊ¾´óÓÚµÈÓÚ1£¿

			// for(int j = 0; j < mUsers.size(); j++) {
			//	 IloConstraint u = cp.ge(dExpr,1);
			//	 }
			cp.setOut(null);

			if (cp.solve()) {
				//mCost = cp.getObjValues()[0];
				mCost = cp.getIncumbentValue(rExpr);
				//mfairness_efficiency = -cp.getObjValues()[1];
				mfairness_efficiency = cp.getObjValue();
				mfairnessdegree = mfairness_efficiency * mCost;
				
				mAllBenefits = cp.getIncumbentValue(dExpr);
				
				//mfairness_efficiency = cp.getObjValue();
			} else {
				System.out.println(" No solution found ");
			}

			cp.end();
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public double getAllUsers() {
		return mAllUsers = mUsers.size();
	}
	
	public double getcost() {
		return mCost;
	}
	
	public double getfairness_degree() {
		return mfairnessdegree;
	}
	
	public double getfairness_efficiency() {
		return mfairness_efficiency;
	}
	public double getallbenefits() {
		return mAllBenefits = (Math.sqrt(mAllBenefits))/mUsers.size();
	}
	public double getbenefit_efficiency() {
		return mbenefitefficiency = mAllBenefits/mCost;
	}
}


/*	public void runnEAD() {
		try {

			IloCP cp = new IloCP();

			IloIntVar[] r = cp.intVarArray(mServersNumber, 0, 1);

			IloLinearNumExpr rExpr = cp.linearNumExpr();
			IloNumExpr dExpr = cp.linearNumExpr();

			IloNumExpr[] sumCoveredUsers = new IloNumExpr[mUsers.size()];

			IloNumExpr[][] userCoverednumber = new IloNumExpr[mUsers.size()][mServersNumber];

			for (int i = 0; i < mServersNumber; i++) {
				rExpr.addTerm(1, r[i]);
				for (int j = 0; j < mUsers.size(); j++) {

					userCoverednumber[j][i] = cp.prod(mUserCovered[i][j], r[i]);
				}
			}

			for (int j = 0; j < mUsers.size(); j++) {
				sumCoveredUsers[j] = cp.max(userCoverednumber[j]);
			}

			dExpr = cp.sum(sumCoveredUsers);

			cp.add(cp.maximize(dExpr));
			
			IloConstraint u = cp.ge(mbudget,rExpr);
			cp.add(u);

			cp.setOut(null);

			if (cp.solve()) {
				
				mAllUsers = cp.getObjValue();
			} else {
				System.out.println(" No solution found ");
			}

			cp.end();
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}
*/
