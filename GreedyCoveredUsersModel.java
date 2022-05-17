package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import objectives.EdgeServer;
import objectives.User;

public class GreedyCoveredUsersModel {
	
	private double mAllUsers;
	//2021
	private double mCost;
	private double mbenefitefficiency;
	private double mAllBenefits;
	private double mAllBenefit_square;
	private int[][] mUserBenefits;
	
	private double mFairness_index;
	private double mfairnessdegree; //make the original value = 0
	private double mfairness_efficiency;
	private List<Integer> mBenefitList;
	//
	private int mServersNumber;
	private int mBudget;

	private int[][] mAdjacencyMatrix;
	private int[][] mUserCovered;
	private List<User> mUsers;
	List<EdgeServer> mServers;
	
	private Map<Integer, Integer> mCoveredUsersMap;
	private List<Integer> mValidUserList;
	private List<Integer> mSelectedServerList;
	private int[][] mDistanceMatrix;
	private int[] user_distribution_result = new int[4];
	//private List<Integer> mBenefitList;
	
	public GreedyCoveredUsersModel(int serversNumber, double fairness_index, int[][] adjacencyMatrix, int[][]distanceMatrics, int[][] userCovered, int[][] userBenefits, List<User> users, List<EdgeServer> servers) 
	{
		mServersNumber = serversNumber;
		mAdjacencyMatrix = adjacencyMatrix;
		mUserCovered = userCovered;
		mUsers = users;
		mServers = servers;
		mDistanceMatrix = distanceMatrics;
		//2021
		mFairness_index = fairness_index;
		mUserBenefits = userBenefits;
		mBenefitList = new ArrayList<>();
		//
		mValidUserList = new ArrayList<>();
		mSelectedServerList = new ArrayList<>();
		//mBenefitList = new ArrayList<>();
		mCoveredUsersMap = new HashMap<>();//?
	}
	
	public void runGreedyCoveredUsers() {
		
		for (int i = 0; i < mServersNumber; i++) {
			mCoveredUsersMap.put(i, mServers.get(i).servedUsers.size());//What is this line mean?
		}
		
		//2021
		//ÕâÀï¸øfairness³õÊ¼»¯ÖµÎªÁã ºÍprivateÎªÁãÓÐÇø±ðÂð..ÎªÊ²Ã´Ñ­»·×ß²»³öÀ´£¿
		mfairnessdegree = 0;
		//
		
		/*while (mSelectedServerList.size() <= mBudget) {
			selectServerWithMaximumCoveredUsers();
		}*/
		
		//µ±²»ÊÇÈ«²¿ÓÃ»§±»¸²¸Ç»òÕßfairnessµÄdegreeÃ»ÓÐ´ïµ½indexÒªÇóÊ±£¬¾Í¼ÌÐøÑ¡ÔñÄÜcover ×î¶àÓÃ»§µÄserver
		while (mValidUserList.size() < mUsers.size()) {
			selectServerWithMaximumCoveredUsers();
			
			
		}	
		
		//2022_calculate mobile user covered results after selected server set has been decided
		calculate_user_covered_results_onetwothree_hops();
		
		//2021
		//ÕâÀï¼ÆËãÒ»ÏÂÈ«²¿µÄcostºÍÈ«²¿µÄbenefitºÍÈ«²¿coverÓÃ»§µÄÊýÁ¿
		mCost = mSelectedServerList.size();
		mAllBenefits = calculateBenefits();//È«²¿ÓÃ»§benefitÖ®ºÍ
		mAllUsers = mValidUserList.size(); //¼ÆËãfairness degreeµÄ·ÖÄ¸
		mAllBenefit_square = calculate_single_Benefits_square(); //fairness degreeµÄ·ÖÄ¸
		
		//ÏÖÔÚ¼ÆËãfairness degree    ÕâÀïfairness_degreeºÍ m fairness degreeÒ»Ñù°É...
		//fairness_degree = (mAllBenefits * mAllBenefits) / (mUsers.size() * mAllBenefit_square);		
		mfairnessdegree = (mAllBenefits * mAllBenefits) / (mUsers.size() * mAllBenefit_square);	
		
		//¼ÆËãfairness efficiency
		//mfairness_efficiency = fairness_degree / mCost;		
		mfairness_efficiency = mfairnessdegree / mCost;
		//
	//	mAllUsers = mValidUserList.size();
	}
	
	private int selectServerWithMaximumCoveredUsers() {
		int maximumCoveredUsers = -1;
		int serverWithMaximumCoveredUsers = -1;
		for (int server : mCoveredUsersMap.keySet()) {
			int coveredUsers = mCoveredUsersMap.get(server);
			if (coveredUsers > maximumCoveredUsers) {
				maximumCoveredUsers = coveredUsers;
				serverWithMaximumCoveredUsers = server;
			}
		}
		
		/*for (int server : mCoveredUsersMap.keySet()) {
			int coveredUsers = mCoveredUsersMap.get(server);
			if (coveredUsers > maximumCoveredUsers) {
				maximumCoveredUsers = coveredUsers;
				serverWithMaximumCoveredUsers = server;
			}
		}*/
		
		//10.08
		 /*int max = 0;        
		 int serverWithMaximumCoveredUsers = 0;        
		 int value=0;        
		 for(int key : mCoveredUsersMap.keySet()){            
			 value = mCoveredUsersMap.get(key);            
			 if(max < value){                
				 max = value;                
				 serverWithMaximumCoveredUsers = key;            
				 }        
		 }*/
		 //mSelectedServerList.add(serverWithMaximumCoveredUsers);
		 //List<Integer> list = new ArrayList<Integer>();
		 //for (int server : mCoveredUsersMap.values()) { 
			// list.add(server);
		 //int maxcovereduserserver = Collections.max(list);
		 //int coveredUsers = mCoveredUsersMap.get(server);
		// }	
		
		//这里就是选择了cover数量最多的server 值是固定的 不是heuristic的解法
		mCoveredUsersMap.remove(serverWithMaximumCoveredUsers);
		mSelectedServerList.add(serverWithMaximumCoveredUsers);
		
		//这里是选好server 之后 把多覆盖到的用户添加到valid用户的列表里
		for (User user : mUsers) {
			if (user.nearEdgeServers.contains(serverWithMaximumCoveredUsers) && !mValidUserList.contains(user.id)) {
				mValidUserList.add(user.id);
			}
		}

		//这里是把选择到的server 的邻接server能cover 到的新的用户 也添加到选择道德用户列表里
		for (int i = 0; i < mServersNumber; i++) {
			if (mAdjacencyMatrix[i][serverWithMaximumCoveredUsers] == 1 || mAdjacencyMatrix[serverWithMaximumCoveredUsers][i] == 1) {
				// mConnectionsMap.remove(i);
				for (User user : mUsers) {
					if (user.nearEdgeServers.contains(i) && !mValidUserList.contains(user.id)) {
						mValidUserList.add(user.id);
					}
				}
			}
		}
		
		return serverWithMaximumCoveredUsers;
	}
	
	//2021
	//Õâ¸öº¯Êý¼ÆËãÁËÈ«²¿ÓÃ»§È«²¿µÄbenefitµÄºÍ È»ºó·µ»ØÁËÈ«²¿µÄbenefit
	private double calculateBenefits() {
		mBenefitList.clear();
		for (int i = 0; i < mUsers.size(); i++) {
			mBenefitList.add(0);
		}

		double benefits = 0;
		
		for (User user : mUsers) {
			for (int server : user.nearEdgeServers) {
				int benefit = 1;
				if (mSelectedServerList.contains(server)) benefit = mUserBenefits[server][user.id];
				if (mBenefitList.get(user.id) < benefit) mBenefitList.set(user.id, benefit);
			}
		}
		
		//Ã¿Ò»¸öbenefit¶¼ÊÇÒ»¸öÓÃ»§µÄbenefit£¬È»ºóbenefitsÊÇÈ«²¿ÓÃ»§µÄbenefitÏà¼Ó
		for (int benefit : mBenefitList) benefits = benefits + benefit;
		
		return benefits;
	}
	
	//½ÓÏÂÀ´Ó¦¸Ã¼ÆËãÃ¿¸öÓÃ»§µÄbenefit£¬È»ºó°Ñbenefit´æÔÚÊý×éÀï£¬×îºóÑ­»·Ã¿¸öÊý×ÖµÄÆ½·½ºÍÏà¼Ó×÷Îªfairness¼ÆËãµÄ·ÖÄ¸
	private double calculate_single_Benefits_square() {
		mBenefitList.clear();
		for (int i = 0; i < mUsers.size(); i++) {
			mBenefitList.add(0);
		}

		double benefits = 0;
		
		for (User user : mUsers) {
			for (int server : user.nearEdgeServers) {
				int benefit = 1;
				if (mSelectedServerList.contains(server)) benefit = mUserBenefits[server][user.id];
				if (mBenefitList.get(user.id) < benefit) mBenefitList.set(user.id, benefit);
			}
		}
		
		//ÕâÀï¼ÆËãbenefitsÊÇÖ¸Ã¿Ò»¸öÓÃ»§µÄbenefitµÄÆ½·½ºÍ,ÕâÑùµÄ»°¿ÉÒÔ·µ»Øbenefits
		for (int benefit : mBenefitList) benefits = benefits + (benefit * benefit);
		
		return benefits;
	}
	//
	
	private int[] calculate_user_covered_results_onetwothree_hops() {		
		//20220510
		for (EdgeServer server : mServers) {
			if (mSelectedServerList.contains(server.id)) {
				
				for (int j = 0; j < mUsers.size(); j++) {
					//0 hop 
					if (mUsers.get(j).nearEdgeServers.contains(server.id)) { //if mUsers's neighbor edge server list contain a server, add distribution result +1, need to break 		
						//0 hop access +1
						user_distribution_result[0]  += 1;
						//need to break, otherwise, the value may increase because there might be some other server cover the user as well
						//break;
					} else if (isConnected(mUsers.get(j).nearEdgeServers, server.id, mAdjacencyMatrix)) { //the server cover the user i is connected with a server in the selected server list
						//1 hop access +1
						user_distribution_result[1]  += 1;
						//break;
					} else if (istwohopsConnected(mUsers.get(j).nearEdgeServers, server.id, mDistanceMatrix)) { //the server cover the user i's distance with a server in the selected server list is 2
						//user j is served via 0 hop
						//userBenefits[i][j] = 4;
						//2 hops access +1
						user_distribution_result[2]  += 1;
						//break;
					} else if (isthreehopsConnected(mUsers.get(j).nearEdgeServers, server.id, mDistanceMatrix)) { //the server cover the user i's distance with a server in the selected server list is 3
						//3 hops access +1
						user_distribution_result[3]  += 1;					
					}
					else {
						//user j is served via more than 3 hops
						//userBenefits[i][j] = 0;
						//user_distribution_result[0] += 1;
						//break;
					}	
					
				}	
				
				break;				
			} 
			//break;				
		}
		
//		user_distribution_result[0] = zerohop_served_users;
//		user_distribution_result[1] = onehop_served_users;
//		user_distribution_result[2] = twohops_served_users;
//		user_distribution_result[3] = threehops_served_users;
		
		return user_distribution_result;
	}
	
	//if the servers are connected 1 hop
		private static boolean isConnected(List<Integer> servers, int server, int[][] adjacencyMatrix) {
			for (int s : servers) {
				if (adjacencyMatrix[s][server] == 1)
					return true;
			}
			return false;
		}
		
		//2 hops
		private static boolean istwohopsConnected(List<Integer> servers, int server, int[][] distanceMatrix) {
			for (int s : servers) {
				if (distanceMatrix[s][server] == 2 || distanceMatrix[server][s] == 2)
					return true;
			}

			return false;
		}
		//3 hops
		private static boolean isthreehopsConnected(List<Integer> servers, int server, int[][] distanceMatrix) {
			for (int s : servers) {
				if (distanceMatrix[s][server] == 3 || distanceMatrix[server][s] == 3)
					return true;
			}
			return false;
		}
	
		public double getAllUsers() {
			//return mAllUsers;
			return user_distribution_result[3];
		}
		
		//202204
		//get user distribution numbers
		public double getAllzeroUsers() {
			//return zerohop_served_users;
			return user_distribution_result[0];
		}
		
		public double getAlloneUsers() {
			return user_distribution_result[1];
		}
		
		public double getAlltwoUsers() {
			return user_distribution_result[2];
		}
		
		public double getAllthreeUsers() {
			return user_distribution_result[2];
		}
		
		//return array (0 hop, 1 hop, 2 hops, 3 hops) 2022
		public int[] get_user_distribution() {
			return user_distribution_result;
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
		return mAllBenefits = mAllBenefits/mValidUserList.size();
	}
	
	public double getbenefit_efficiency() {
		return mbenefitefficiency = mAllBenefits/mCost;
	}
}
