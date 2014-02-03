package edu.dartmouth.cs.myruns5;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class FriendsFragment extends ListFragment{

	Toast m_currentToast;
	final int MAX_ADDED_FRIENDS = 4;
	private int numAddedFriends;
	private boolean canAddFriend;
	private ArrayList<Integer> selectedItems; // Used to figure which index of fbFriends to access
	private ArrayList<ParseUser> fbFriends; // Stores all Facebook friends that user is Friends with
	private ArrayList<String> friends; //Stores all Facebook friends that use UV Guardian
	private ParseUser currentUser; 
	ArrayAdapter<String> adapter;

	/*
	public ArrayList<Integer> getSelectedItems() {
		return selectedItems;
	}
	
	public ArrayList<ParseUser> getFBFriends() {
		return fbFriends;
	}
	*/
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout friendsMain = (LinearLayout)inflater.inflate(R.layout.fragment_friends, container, false);
		
		canAddFriend = true;
		
		Button showGraph = (Button) friendsMain.findViewById(R.id.button_show_graph);
        showGraph.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Show bar graph on click
           	 	showGraphActivity();
            }
        });
		
		//Initialize Parse for grabbing Facebook friends
		Parse.initialize(getActivity(), "2zU6YnzC8DLSMJFuAOiLNr3MD6X0ryG52mZsxoo0", "m4rlzlSWyUvgcEkNULlVqRBlsX2iGRilskltCqYG");
		ParseFacebookUtils.initialize(((Integer)R.string.app_id).toString());
		
		selectedItems = new ArrayList<Integer>();
		fbFriends = new ArrayList<ParseUser>();
		currentUser = ParseUser.getCurrentUser();
		friends = new ArrayList<String>();
		numAddedFriends = 0;
		
		return friendsMain;
	}
	
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    
    getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
        
        // Puts the position of the item that was selected into selectedItem
        // Toggles whether or not a friend was added or not
    	@Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    		
    		//In case the List Fragment is populated with no friends, prevent adding friends
    		if(!canAddFriend) {
    			return true;
    		}
    		
        	if(selectedItems.contains((Integer)position)){
        		selectedItems.remove((Integer)position);
        		friends.set(position,friends.get(position).substring(0,friends.get(position).indexOf(" (+)")));
        		adapter.notifyDataSetChanged();
        		showToast("Friend Removed!");
        		numAddedFriends--;
        	}
        	else if(numAddedFriends<MAX_ADDED_FRIENDS) {
        		selectedItems.add(position);
        		friends.set(position,friends.get(position)+" (+)");
        		adapter.notifyDataSetChanged();
        		showToast("Friend added to chart!");
        		numAddedFriends++;
        	}
        	else {
        		showToast("A maximum of " + MAX_ADDED_FRIENDS + " friends can be graphed!");
        	}
            return true;
        }
    });

    if(currentUser!=null) {
    	canAddFriend = true;
    	fbFriends = (ArrayList<ParseUser>)currentUser.get("fb_friends");
    	
    	// Adds all the names to populate the friends List Fragment
    	for(ParseUser user : fbFriends) {
    		//No internet connectivity may lead to crash here (accessing Parse DB); implement check?
    		friends.add((String)user.get("name"));
    	}
    }
    else {
    	//Not logged in, so do nothing and disable adding friends to graph
    	canAddFriend = false;
    	friends.add("NO FRIENDS :[");
    }
    
	adapter = new ArrayAdapter<String>(getActivity(), 
			android.R.layout.simple_list_item_1, friends);
	setListAdapter(adapter);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    // do nothing (do not add friend) on single click 
  }
  
  private void showToast(String text) {
	  //Get rid of current toast before showing the next message
      if(m_currentToast != null)
          m_currentToast.cancel();
      m_currentToast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
      m_currentToast.show();
  }
  
  	//Switches Intent to Bar Graph
	//Passes along the array of selected friend indexes
	private void showGraphActivity() {
		Intent intent = new Intent(getActivity(),BarGraph.class);
		intent.putExtra("selectedItems", selectedItems);
		
		//No need for put/get Extra's in terms of ParseUser Facebook friends
		//BarGraph regrabs Facebook friends using the current Parse user to prevent misuse of Serialization 
		//intent.putExtra("fbFriends", fbFriends);
		
		startActivity(intent);
	}
	
  
} 