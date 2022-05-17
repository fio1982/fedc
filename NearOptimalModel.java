package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import objectives.EdgeServer;
import objectives.User;

public class NearOptimalModel {

	private double mAllUsers;

	private int mServersNumber;
	private int mBudget;
	private int[][] mAdjacencyMatrix;
	private int[][] mDistanceMatrix;
	private int[][] mUserCovered;
	private List<User> mUsers;
	List<EdgeServer> mServers;

	// 2021
	private double mCost = 0;
	private double mbenefitefficiency;
	private double mAllBenefits;
	private double mAllBenefit_square;
	private int[][] mUserBenefits;

	private double mFairness_index;
	private double mfairnessdegree; // make the original value = 0
	private double mfairness_efficiency;
	private List<Integer> mBenefitList;
	private List<Integer> mServerfairnesslist;
	//

	private Map<Integer, Integer> mCoveredUsersMap;
	private List<Integer> mValidUserList;
	
	//202204_creat user lists to store users number within 1 hop/ 2 hops/ 3 hops for the major revision comments
	private List<Integer> mValidUserList_zero_hop;
	private List<Integer> mValidUserList_one_hops;
	private List<Integer> mValidUserList_two_hops;
	private List<Integer> mValidUserList_three_hop;
//	//202204
//	private int zerohop_served_users;
//	private int onehop_served_users;
//	private int twohops_served_users;
//	private int threehops_served_users;
	
	private int[] user_distribution_result = new int[5];

	
	private List<Integer> mSelectedServerList;

	// 2021
	private List<Integer> bubbleValidUserList;
	private List<Integer> bubbleSelectServerList;

	public NearOptimalModel(int serversNumber, double fairness_index, int[][] adjacencyMatrix, int[][]distanceMatrics, int[][] userCovered,
			int[][] userBenefits, List<User> users, List<EdgeServer> servers) {
		mServersNumber = serversNumber;
		mAdjacencyMatrix = adjacencyMatrix;
		mDistanceMatrix = distanceMatrics;
		mUserCovered = userCovered;
		mUsers = users;
		mServers = servers;

		// 2021
		mFairness_index = fairness_index;
		mUserBenefits = userBenefits;
		mBenefitList = new ArrayList<>();
		//

		mValidUserList = new ArrayList<>();
		
		//
		mSelectedServerList = new ArrayList<>();
		mCoveredUsersMap = new HashMap<>();// ?
	}

	public void runNearOptimal() {

		for (int i = 0; i < mUsers.size(); i++)
			mBenefitList.add(0);

		mAllBenefits = 0;

		while (mValidUserList.size() < mUsers.size()) {
//			updateNewUsersCoveredByServerList();
			bubbleServerWithMaximumIncreasingUncoveredUsers();

			// ÕâÀïÑ¡ÔñÁË¼ÆËã³öfairness efficiency×î´óµÄÄÇ¸öserver µÄ id
			
			//
			//mValidUserList.addAll(mServers.get(selected).getCoveredUsersWithinOneHopNotInTheList(mValidUserList));
			//mValidUserList.addAll(mServers.get(selected).getDirectlyCoveredUsersNotInTheList(mValidUserList));

			
			//
			// for(int i = 0; i < mServers.size();i++) {
			// if (mServers.get(i).id == selected) {
			// mServers.remove(i);
			// i--;
			// }
			// }

//			System.out.println(mfairnessdegree + " --- " + mfairness_efficiency);

		}
		
		//2022_calculate mobile user covered results after selected server set has been decided
		calculate_user_covered_results_onetwothree_hops();
		
		// 2021
					// ÕâÀï¼ÆËãÒ»ÏÂÈ«²¿µÄcostºÍÈ«²¿µÄbenefitºÍÈ«²¿coverÓÃ»§µÄÊýÁ¿
					mCost = mSelectedServerList.size();
					mAllBenefits = calculateBenefits();// È«²¿ÓÃ»§benefitÖ®ºÍ
					mAllUsers = mValidUserList.size(); // ¼ÆËãfairness degreeµÄ·ÖÄ¸
					mAllBenefit_square = calculate_single_Benefits_square(); // fairness degreeµÄ·ÖÄ¸

					// ÏÖÔÚ¼ÆËãfairness degree ÕâÀïfairness_degreeºÍ m fairness degreeÒ»Ñù°É...
					// fairness_degree = (mAllBenefits * mAllBenefits) / (mUsers.size() *
					// mAllBenefit_square);
					mfairnessdegree = (mAllBenefits * mAllBenefits) / (mUsers.size() * mAllBenefit_square);
//					//20210816
//					mAllBenefits = mAllBenefits/mValidUserList.size();
					// ¼ÆËãfairness efficiency
					// mfairness_efficiency = fairness_degree / mCost;
					mfairness_efficiency = mfairnessdegree / mCost;
		//20210223
	/*	for (int i : mSelectedServerList)
			System.out.print(i + ", ");

		System.out.println();
	 */
		// for (int serverId : mSelectedServerList) {
		// for (int userId : mServers.get(serverId).servedUsers) {
		// mBenefitList.set(userId, 2);
		// }
		// for (int userId : mServers.get(serverId).usersInOneHop) {
		// if (mBenefitList.get(userId) < 1) mBenefitList.set(userId, 1);
		// }
		// }

		// System.out.print("Benefits list: ");
		// for (int benefits : mBenefitList) {
		// mAllBenefits = mAllBenefits + benefits;
		// //System.out.print(benefits + " ");
		// }
		// System.out.println();

		// mCost = mSelectedServerList.size();
		// mBenefitPerReplica = mAllBenefits / mCost;
		// mAllUsers = mValidUserList.size(); // ¼ÆËãfairness degreeµÄ·ÖÄ¸
	}

//	private boolean hasIncreaseInFairnessDegree() {
//		
//		//TODO: calculate fairness degree
//		double currentFDegree = 0;
//		
//		for (EdgeServer server : mServers) {
//			if (mSelectedServerList.contains(server.id)) continue;
//			
//			mSelectedServerList.add(server.id);
//			//TODO: calculate fairness degree
//			double newFDegree = 0;
//			
//			if (newFDegree > currentFDegree) return true;
//		}
//		
//		return false;
//	}

	private int[] calculate_user_covered_results_onetwothree_hops() {		
		//202204

//		for (EdgeServer server : mServers) {
//			if (mSelectedServerList.contains(server.id)) {
//				
//				for (int j = 0; j < mUsers.size(); j++) {
//					if (mUsers.get(j).nearEdgeServers.contains(server.id)) {
//						//user j is served via 0 hop
//						//userBenefits[i][j] = 4;
//						zerohop_served_users += 1;
//						break;
//					}else if (isConnected(mUsers.get(j).nearEdgeServers, server.id, mAdjacencyMatrix)) {
//						//user j is served via 1 hop
//						//userBenefits[i][j] = 3;
//						onehop_served_users += 1;
//						break;
//					}else if(istwohopsConnected(mUsers.get(j).nearEdgeServers, server.id, mDistanceMatrix)) {
//						//user j is served via 2 hops
//						//userBenefits[i][j] = 2;
//						twohops_served_users += 1;
//						break;
//					}else if(isthreehopsConnected(mUsers.get(j).nearEdgeServers, server.id, mDistanceMatrix)) {
//						//user j is served via 3 hops
//						//userBenefits[i][j] = 1;
//						threehops_served_users += 1;
//						break;
//					}else {
//						//user j is served via more than 3 hops
//						//userBenefits[i][j] = 0;
//						break;
//					}							
//				}				
//			}				
//		}
//		int zerohop_served_users;
//	 	int onehop_served_users;
//		int twohops_served_users;
//		int threehops_served_users;

//
		
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
						user_distribution_result[0]  += 0;
						user_distribution_result[0]  += 0;
						user_distribution_result[0]  += 0;
						user_distribution_result[0]  += 0;
						user_distribution_result[4]  += 1;
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
	
	private int bubbleServerWithMaximumIncreasingUncoveredUsers() {
		int id = -1;
		double cu = 0.00;
		/*
		 * double dcu = 0.00; double alluserbenefit = 0; double alluserbenefitsquare =
		 * 0; double allselectedservercost = 0; double fairness = 0; double
		 * fairnessefficiency = 0; double bubblefe = 0; List<Integer>
		 * bubbleValidUserList = mValidUserList; bubbleSelectServerList =
		 * mSelectedServerList;
		 */

		// mServerfairnesslist = new ArrayList<>();
		for (EdgeServer server : mServers) {
			if (mSelectedServerList.contains(server.id))
				continue;

			double newcu = server.getnewfairnessWithinOneHopNotInTheListAndDirectCovered(mValidUserList, mSelectedServerList,
					server.id, mUserBenefits, mUsers);
			if (newcu > cu)
			{
				cu = newcu; // Ò»ÌøÄÚÐÂÔö¼ÓµÄÓÃ»§
				// dcu = server.getDirectlyCoveredUsersNotInTheList(mValidUserList).size(); //
				id = server.id;
			}
		}
		
		mSelectedServerList.add(id);
		
		//这里是选好server 之后 把多覆盖到的用户添加到valid用户的列表里
				for (User user : mUsers) { 
					if (user.nearEdgeServers.contains(id) && !mValidUserList.contains(user.id)) {
						mValidUserList.add(user.id);
					}
				}

				//这里是把选择到的server 的邻接server能cover 到的新的用户 也添加到选择de用户列表里
				for (int i = 0; i < mServersNumber; i++) {
					if (mAdjacencyMatrix[i][id] == 1 || mAdjacencyMatrix[id][i] == 1) {
						// mConnectionsMap.remove(i);
						for (User user : mUsers) {
							if (user.nearEdgeServers.contains(i) && !mValidUserList.contains(user.id)) {
								mValidUserList.add(user.id);
							}
						}
					}
				}
		return id;
	}

	private double calculateBenefits() {
		mBenefitList.clear();
		for (int i = 0; i < mUsers.size(); i++) {
			mBenefitList.add(0);
		}

		double benefits = 0;

		for (User user : mUsers) {
			for (int server : user.nearEdgeServers) {
				int benefit = 1;
				if (mSelectedServerList.contains(server))
					benefit = mUserBenefits[server][user.id];
				if (mBenefitList.get(user.id) < benefit)
					mBenefitList.set(user.id, benefit);
			}
		}

		// Ã¿Ò»¸öbenefit¶¼ÊÇÒ»¸öÓÃ»§µÄbenefit£¬È»ºóbenefitsÊÇÈ«²¿ÓÃ»§µÄbenefitÏà¼Ó
		for (int benefit : mBenefitList)
			benefits = benefits + benefit;

		return benefits;
	}

	// ½ÓÏÂÀ´Ó¦¸Ã¼ÆËãÃ¿¸öÓÃ»§µÄbenefit£¬È»ºó°Ñbenefit´æÔÚÊý×éÀï£¬×îºóÑ­»·Ã¿¸öÊý×ÖµÄÆ½·½ºÍÏà¼Ó×÷Îªfairness¼ÆËãµÄ·ÖÄ¸
	private double calculate_single_Benefits_square() {
		mBenefitList.clear();
		for (int i = 0; i < mUsers.size(); i++) {
			mBenefitList.add(0);
		}

		double benefits = 0;

		for (User user : mUsers) {
			for (int server : user.nearEdgeServers) {
				int benefit = 1;
				if (mSelectedServerList.contains(server))
					benefit = mUserBenefits[server][user.id];
				if (mBenefitList.get(user.id) < benefit)
					mBenefitList.set(user.id, benefit);
			}
		}

		// ÕâÀï¼ÆËãbenefitsÊÇÖ¸Ã¿Ò»¸öÓÃ»§µÄbenefitµÄÆ½·½ºÍ,ÕâÑùµÄ»°¿ÉÒÔ·µ»Øbenefits
		for (int benefit : mBenefitList)
			benefits = benefits + (benefit * benefit);

		return benefits;
	}

	public double getAllUsers() {
		//return mAllUsers;
		return user_distribution_result[4];
	}
	
	//202204
	//get user distribution numbers
	public double getAllzeroUsers() {
		//return zerohop_served_users;
		return user_distribution_result[1];
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
		//return mbenefitefficiency = (mAllBenefits/mValidUserList.size())/mCost;
		return mbenefitefficiency = mAllBenefits/mCost;
	}
}

//	if (server.getCoveredUsersWithinOneHopNotInTheList(mValidUserList).size() > cu ||
//			(server.getCoveredUsersWithinOneHopNotInTheList(mValidUserList).size() == cu
//					&& server.getDirectlyCoveredUsersNotInTheList(mValidUserList).size() > dcu)
//			) {
//		cu = server.getCoveredUsersWithinOneHopNotInTheList(mValidUserList).size();
//		dcu = server.getDirectlyCoveredUsersNotInTheList(mValidUserList).size();
//		id = server.id;
//	}
// }

//		mAllBenefits = 0;

// mSelectedServerList.add(server);
// mValidUserList.addAll(mServers.get(server).getCoveredUsersWithinOneHopNotInTheList(mValidUserList));
// 2021
// ÕâÀï¼ÆËãÒ»ÏÂÈ«²¿µÄcostºÍÈ«²¿µÄbenefitºÍÈ«²¿coverÓÃ»§µÄÊýÁ¿

// ¶ÔÓÚÃ¿¸öÔÚserver listÀïµÄserver£¬Òª¼ÆËãÑ¡ÔñÕâ¸öserverÖ®ºó£¬¶ÔÓÚËùÓÐm-validuserlistÀï
// Ã»ÓÐ°üÀ¨µÄuser¼ÓÈëµ½ÐÂ½¨µÄuserlistÀï¡£ È»ºó·µ»ØÕâ¸öuser list¡£
// ÐÂµÄnear optimal·½·¨°ÑÓÃ»§·ÖÎªÁ½×é Ò»×éÊÇÐÂÔö¼ÓµÄÒ»ÌøµÄÓÃ»§£¬Ò»×éÊÇ0ÌøµÄÓÃ»§
// edge serverÀïÃæº¯Êý·µ»ØµÄÓ¦¸ÃÖ±½ÓÊÇ Ñ¡ÔñÄ³¸öserverÖ®ºó£¬ µ÷ÓÃµÄº¯ÊýÀïÒª¼ÆËãÈ«²¿µÄcoverµÄÓÃ»§µÄlist£¬È»ºó¼ÆËãbenefit
// list£¨ÐÂ½¨Ò»¸ö£©£¬µ÷ÓÃ¶îÍâÁ½¸öº¯Êý·Ö±ð¼ÆËãbenefitµÄºÍ ºÍ benefitÆ½·½µÄºÍ¡£
// ·µ»ØµÄÖµ ÔÚ º¯ÊýÀï¼ÆËãÑ¡ÔñÕâ¸öserverÖ®ºóµÄfairness
// µÄÖµ£¬ÕâÑùµÄ»°¾Í²»ÓÃ°ÑÓÃ»§·ÖÎªÁ½²¿·ÖÁË....·µ»ØµÄÊ±ºò¡£Ö»ÐèÒªÑ¡ÔñÁËÕâ¸öserverÖ®ºóµÄserver listÊµÏÖµÄfairness×î´ó£¬¾ÍÓÃÕâ¸öid
// ÕâÑùÐèÒª´«µÝÁ½¸ölist£¬Ò»¸öÊÇmValidUserList£¬Ò»¸öÊÇmSelectedServerList
//		mCost = mSelectedServerList.size();
//		mAllBenefits = calculateBenefits();//È«²¿ÓÃ»§benefitÖ®ºÍ
//		mAllUsers = mValidUserList.size(); //¼ÆËãfairness degreeµÄ·ÖÄ¸
//		mAllBenefit_square = calculate_single_Benefits_square(); //fairness degreeµÄ·ÖÄ¸
//		
//		//ÏÖÔÚ¼ÆËãfairness degree    ÕâÀïfairness_degreeºÍ m fairness degreeÒ»Ñù°É...
//		//fairness_degree = (mAllBenefits * mAllBenefits) / (mUsers.size() * mAllBenefit_square);		
//		mfairnessdegree = (mAllBenefits * mAllBenefits) / (mUsers.size() * mAllBenefit_square);	
//		
//		//¼ÆËãfairness efficiency
//		//mfairness_efficiency = fairness_degree / mCost;		
//		mfairness_efficiency = mfairnessdegree / mCost;
//		//	

//Õâ¸öº¯Êý¼ÆËãÁËÈ«²¿ÓÃ»§È«²¿µÄbenefitµÄºÍ È»ºó·µ»ØÁËÈ«²¿µÄbenefit
