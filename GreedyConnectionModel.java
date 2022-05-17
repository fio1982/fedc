package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import objectives.EdgeServer;
import objectives.User;

public class GreedyConnectionModel {

	private int mServersNumber;
	private int mBudget;
	//2021
	private double mCost;
	private double mbenefitefficiency;
	private double mAllBenefits;
	private double mAllBenefit_square;
	private int[][] mUserBenefits;
	private int[][] mDistanceMatrix;
	
	private double mFairness_index;
	private double mfairnessdegree; //make the original value = 0
	private double mfairness_efficiency;
	private List<Integer> mBenefitList;
	//
	private int[][] mAdjacencyMatrix;
	private int[][] mUserCovered;
	private List<User> mUsers;
	List<EdgeServer> mServers;
	
	private double mAllUsers;

	private List<Integer> mValidUserList;

	private Map<Integer, Integer> mConnectionsMap;
	private List<Integer> mSelectedServerList;
	
	private int[] user_distribution_result = new int[4];

	public GreedyConnectionModel(int serversNumber, double fairness_index, int[][] adjacencyMatrix, int[][]distanceMatrics, int[][] userCovered, int[][] userBenefits, List<User> users, List<EdgeServer> servers) {
		mServersNumber = serversNumber;
		mAdjacencyMatrix = adjacencyMatrix;
		mUserCovered = userCovered;
		mUsers = users;
		mValidUserList = new ArrayList<>();
		mSelectedServerList = new ArrayList<>();
		mConnectionsMap = new HashMap<>();
		
		//2021
		mFairness_index = fairness_index;
		mUserBenefits = userBenefits;
		mBenefitList = new ArrayList<>();
		//
		mServers = servers;
		mDistanceMatrix = distanceMatrics;
	}

	public void runGreedyConnection() {
		for (int i = 0; i < mServersNumber; i++) {
			int connection = 0;
			for (int c : mAdjacencyMatrix[i])
				connection = connection + c;
			mConnectionsMap.put(i, connection);
		}

		//2021
		//这里给fairness初始化值为零 和private为零有区别吗..为什么循环走不出来？
		mfairnessdegree = 0;
		//
		
		/*while (mSelectedServerList.size() <= mBudget) {
			selectServerWithMaximumConnections();
		}
		*/
		
		while (mValidUserList.size() < mUsers.size()) {
			selectServerWithMaximumConnections();
			
			
			//
		}	
		
		
		//2022_calculate mobile user covered results after selected server set has been decided
		calculate_user_covered_results_onetwothree_hops();
		
		//2021
		//这里计算一下全部的cost和全部的benefit和全部cover用户的数量
		mCost = mSelectedServerList.size();
		mAllBenefits = calculateBenefits();//全部用户benefit之和
		mAllUsers = mValidUserList.size(); //计算fairness degree的分母
		mAllBenefit_square = calculate_single_Benefits_square(); //fairness degree的分母
		
		//现在计算fairness degree    这里fairness_degree和 m fairness degree一样吧...
		//fairness_degree = (mAllBenefits * mAllBenefits) / (mUsers.size() * mAllBenefit_square);		
		mfairnessdegree = (mAllBenefits * mAllBenefits) / (mUsers.size() * mAllBenefit_square);	
		
		//计算fairness efficiency
		//mfairness_efficiency = fairness_degree / mCost;		
		mfairness_efficiency = mfairnessdegree / mCost;

		//mAllUsers = mValidUserList.size();
	}
	/*
	private double calculateUsers() {

		mValidUserList.clear();
		for (int i = 0; i < mUsers.size(); i++) {
			mValidUserList.add(0);
		}

		double validusers = 0;

		for (User user : mUsers) {
			for (int server : user.nearEdgeServers) {
				int validuser = 1;
				if (mSelectedServerList.contains(server))
					validuser = mUserCovered[server][user.id];
				if (mValidUserList.get(user.id) < validuser)
					mValidUserList.set(user.id, validuser);
			}
		}

		for (int validuser : mValidUserList)
			validusers = validusers + validuser;

		return validusers;
	}*/

	private int selectServerWithMaximumConnections() { 
		int maximumConnections = 0;
		int serverWithMaximumConnections = -1;
		for (int server : mConnectionsMap.keySet()) {
			int connections = mConnectionsMap.get(server);
			if (connections > maximumConnections) {
				maximumConnections = connections;
				serverWithMaximumConnections = server;
			}
		}
		mConnectionsMap.remove(serverWithMaximumConnections);
		mSelectedServerList.add(serverWithMaximumConnections);
		
		for (User user : mUsers) {
			if (user.nearEdgeServers.contains(serverWithMaximumConnections) && !mValidUserList.contains(user.id)) {
				mValidUserList.add(user.id);
			}
		}

		for (int i = 0; i < mServersNumber; i++) {
			if (mAdjacencyMatrix[i][serverWithMaximumConnections] == 1 || mAdjacencyMatrix[serverWithMaximumConnections][i] == 1) {
				// mConnectionsMap.remove(i);
				for (User user : mUsers) {
					if (user.nearEdgeServers.contains(i) && !mValidUserList.contains(user.id)) {
						mValidUserList.add(user.id);
					}
				}
			}
		}

		return serverWithMaximumConnections;
	}

	//2021
	//这个函数计算了全部用户全部的benefit的和 然后返回了全部的benefit
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
		
		//每一个benefit都是一个用户的benefit，然后benefits是全部用户的benefit相加
		for (int benefit : mBenefitList) benefits = benefits + benefit;
		
		return benefits;
	}
	
	//接下来应该计算每个用户的benefit，然后把benefit存在数组里，最后循环每个数字的平方和相加作为fairness计算的分母
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
		
		//这里计算benefits是指每一个用户的benefit的平方和,这样的话可以返回benefits
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
