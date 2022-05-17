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

public class LiuICWSModel {

	private double mAllUsers;
	private double mCost;
	private double mbenefitefficiency;
	private double mAllBenefits;
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
	
	public LiuICWSModel (int serversNumber, double fairness_index, int[][] adjacencyMatrix, int[][] userCovered, int[][] userBenefits, List<User> users, 
			List<EdgeServer> servers) {
		mServersNumber = serversNumber;
		mFairness_index = fairness_index;
		mAdjacencyMatrix = adjacencyMatrix;
		
		//这里是benefit
	//	mUserCovered = userCovered;
		mUsers = users;
		
		//这里是benefit
		mUserBenefits = userCovered;
		mValidUserList = new ArrayList<>();
		mSelectedServerList = new ArrayList<>();
		
		//mUserList = new ArrayList<>();
		mBenefitList = new ArrayList<>();	
	}
	
	public void runLiuICWS() {
		
		try {
			//new cplex objective
			IloCP cp = new IloCP();
			
			//r 是caching变量 0/1
			IloIntVar[] r = cp.intVarArray(mServersNumber, 0, 1);
			
			//线性变量 有啥用啊？
			IloNumExpr[] eExpr = new IloNumExpr[3];
			
			//第二个线性变量 全部用户benefit的和
			IloLinearNumExpr rExpr = cp.linearNumExpr();
			//第二个线性变量 全部用户benefit的和
			IloNumExpr dExpr = cp.linearNumExpr();
			
			//第三个线性变量 全部用户benefit的平方的和
			IloNumExpr sExpr = cp.linearNumExpr();
			
			//
			IloNumExpr ddExpr = cp.linearNumExpr();
			
			//
			IloNumExpr jainExpr = cp.linearNumExpr();
			
			//全部被cover的用户的数量，长度是user数量，最后求和
			IloNumExpr[] maxBenifitsExprs = new IloNumExpr[mUsers.size()];
			
			//这里设置一个一维数组，存储每个benefit的平方
			IloNumExpr[] maxSquareBenifitsExprs = new IloNumExpr[mUsers.size()];
			
			//用户和server之间cover关系的二维数组
			IloNumExpr[][] userBenefitsExprs = new IloNumExpr[mUsers.size()][mServersNumber];

			//这里是计算用户cover的二维数组 和caching的一维数组
			for (int i = 0; i < mServersNumber; i++) {
				rExpr.addTerm(1, r[i]);
				for (int j = 0; j < mUsers.size(); j++) {
					userBenefitsExprs[j][i] = cp.prod(mUserBenefits[i][j], r[i]);
				}
			}

			//这里计算全部的benefit 一维数组
			for (int j = 0; j < mUsers.size(); j++) {
				maxBenifitsExprs[j] = cp.max(userBenefitsExprs[j]);
			}

			//这里是线性变量 全部benefit的总和
			dExpr = cp.sum(maxBenifitsExprs);
			
			ddExpr = cp.square(dExpr);
		
			
			//这里 maxSquare数组存储每个用户benefit的平方
			for (int j = 0; j < mUsers.size(); j++) {
				maxSquareBenifitsExprs[j] = cp.prod(maxBenifitsExprs[j],maxBenifitsExprs[j]);
			}
			
			//这里是每个benefit的平方的和 分母
			sExpr = cp.sum(maxSquareBenifitsExprs);
			sExpr = cp.prod(sExpr, mUsers.size());
			
			jainExpr = cp.quot(ddExpr, sExpr);
			
			eExpr[0] = rExpr;
			eExpr[1] = cp.negative(dExpr);
			eExpr[2] = cp.negative(sExpr);

			IloMultiCriterionExpr moExpr = cp.staticLex(eExpr);

			cp.add(cp.minimize(moExpr));
			//cp.add(cp.maximize(dExpr));

		//	IloConstraint u = cp.ge(dExpr,(mUsers.size()*2));//用户benefit的值是两倍cover用户数量的值 所以 大于等于两倍全部用户size
			IloConstraint u = cp.ge(dExpr,mUsers.size());//用户benefit的值是两倍cover用户数量的值 所以 大于等于两倍全部用户size
		//	IloConstraint u = cp.ge(jainExpr, 0.75);
		//	for (int i = 0; i < mUsers.size(); i++) {
		//		u = cp.and(u, cp.ge(maxBenifitsExprs[i], 1)); //每个用户的benefit都至少为1
		//	}
		//	IloConstraint u = cp.ge(mfairnessdegree,0.95);
			cp.add(u);
			cp.setOut(null);

			if (cp.solve()) {	
				mAllUsers = cp.getObjValue();
				mCost = cp.getObjValues()[0];
				mAllBenefits = -cp.getObjValues()[1];
				mSquare = -cp.getObjValues()[2];
				mfairnessdegree = (mAllBenefits * mAllBenefits) / mSquare;
				mfairness_efficiency = mfairnessdegree / mCost;
				mAllUsers = mAllBenefits;
				
				/*mCost = cp.getIncumbentValue(rExpr);
				mAllBenefits = cp.getObjValue();		
				mfairnessdegree = cp.getIncumbentValue(jainExpr);
				mfairness_efficiency = mfairnessdegree / mCost;
				mAllUsers = mAllBenefits;*/
				
			} else {
				System.out.println(" No solution found ");
			}
			cp.end();
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}
	
	//不能加入除法运算啊
//	private double calculateBenefits(IloNumExpr a, IloNumExpr b) {
//		double c;
//		c = a/b;
//		return c;
//	}
	
	public double getAllUsers() {
		return mAllUsers;
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
		return mAllBenefits = mAllBenefits/100;
	}
	
	public double getbenefit_efficiency() {
		return mbenefitefficiency = mAllBenefits/mCost;
	}
	
}

/*
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

public class LiuICWSModel {

	private double mAllUsers;
	private double mCost;
	private double mAllBenefits;
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
	
	public LiuICWSModel (int serversNumber, double fairness_index, int[][] adjacencyMatrix, int[][] userCovered, int[][] userBenefits, List<User> users, 
			List<EdgeServer> servers) {
		mServersNumber = serversNumber;
		mFairness_index = fairness_index;
		mAdjacencyMatrix = adjacencyMatrix;
		
		//这里是benefit
	//	mUserCovered = userCovered;
		mUsers = users;
		
		//这里是benefit
		mUserBenefits = userCovered;
		mValidUserList = new ArrayList<>();
		mSelectedServerList = new ArrayList<>();
		
		//mUserList = new ArrayList<>();
		mBenefitList = new ArrayList<>();	
	}
	
	public void runLiuICWS() {
		
		try {
			//new cplex objective
			IloCP cp = new IloCP();
			
			//r 是caching变量 0/1
			IloIntVar[] r = cp.intVarArray(mServersNumber, 0, 1);
			
			//线性变量 有啥用啊？
			IloLinearNumExpr rExpr = cp.linearNumExpr();
			
			//第二个线性变量 全部用户benefit的和
			IloNumExpr dExpr = cp.linearNumExpr();
			
			//第三个线性变量 全部用户benefit的平方的和
			IloNumExpr sExpr = cp.linearNumExpr();
			
			//变量存储二分之一的benefit  这里能代表全部cover的用户数量吗？ 因为cover了benefit是2
			IloNumExpr uExpr = cp.linearNumExpr();
			
			//最终的优化
			IloNumExpr oExpr = cp.linearNumExpr();
			
			//全部被cover的用户的数量，长度是user数量，最后求和
			IloNumExpr[] maxBenifitsExprs = new IloNumExpr[mUsers.size()];
			
			//这里设置一个一维数组，存储每个benefit的平方
			IloNumExpr[] maxSquareBenifitsExprs = new IloNumExpr[mUsers.size()];
			
			//用户和server之间cover关系的二维数组
			IloNumExpr[][] userBenefitsExprs = new IloNumExpr[mUsers.size()][mServersNumber];

			//这里是计算用户cover的二维数组 和caching的一维数组
			for (int i = 0; i < mServersNumber; i++) {
				rExpr.addTerm(1, r[i]);
				for (int j = 0; j < mUsers.size(); j++) {
					userBenefitsExprs[j][i] = cp.prod(mUserBenefits[i][j], r[i]);
				}
			}

			//这里计算全部的benefit 一维数组
			for (int j = 0; j < mUsers.size(); j++) {
				maxBenifitsExprs[j] = cp.max(userBenefitsExprs[j]);
			}

			//这里是线性变量 全部benefit的总和
			dExpr = cp.sum(maxBenifitsExprs);
			
			//这里计算用户数量  二分之一的benefit
			uExpr = cp.prod(dExpr, 0.5);
			
			//全部benefit的和的平方 分子
			dExpr = cp.prod(dExpr, dExpr);

			//这里 maxSquare数组存储每个用户benefit的平方
			for (int j = 0; j < mUsers.size(); j++) {
				maxSquareBenifitsExprs[j] = cp.prod(maxBenifitsExprs[j],maxBenifitsExprs[j]);
			}
			
			//这里是每个benefit的平方的和 分母
			sExpr = cp.sum(maxSquareBenifitsExprs);
			
            //现在把benefit平方的和乘以全部用户的数量 作为fairness的分母
			//sExpr = uExpr/sExpr;
			//oExpr = calculateBenefits(dExpr, sExpr);
			//oExpr = cp.div(dExpr, sExpr);
			cp.add(cp.maximize(dExpr));
			
			IloConstraint u = cp.ge(mbudget,rExpr);//rExpr 小于等于budget 说明选择caching的server数量小于等于budget
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
	
	//不能加入除法运算啊
//	private double calculateBenefits(IloNumExpr a, IloNumExpr b) {
//		double c;
//		c = a/b;
//		return c;
//	}
	
	
	public double getAllUsers() {
		return mAllUsers;
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
	
}
*/
