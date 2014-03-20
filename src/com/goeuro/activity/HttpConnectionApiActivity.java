package com.goeuro.activity;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.goeuro.R;
import com.goeuro.activity.GPSTracker;
import com.goeuro.http.HttpHandler;
import com.goeuro.http.HttpListner;



public class HttpConnectionApiActivity extends Activity implements HttpListner, TextWatcher, OnClickListener{
    /** Called when the activity is first created. */
	Handler handler = null;
	TextView textviewResponse;
	ProgressDialog progressDialog;
	EditText edtTextUrl = null;
	JSONArray results_Array;
	double current_latitude ;
	double current_longitude;
	int the_caller; // identify with text_field called the JSON 
	  Spinner spinner;
	
	AutoCompleteTextView from_txt,to_txt;
	ArrayAdapter<String> adapter;
	//for the calendar
	private ImageButton search_button,calendar_button;
	private Calendar cal;
	private int day;
	private int month;
	private int year;
	private EditText et;

	
	
	//for current geo_location
		GPSTracker gps;
		 ArrayList<String> lang_List = new ArrayList<String>();

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //language list 
        spinner = (Spinner) findViewById(R.id.lang_spinner); 
        lang_List.add("en");
        lang_List.add("de");
        lang_List.add("sp");
        lang_List.add("fr");
        // Create the ArrayAdapter
        ArrayAdapter<CharSequence> lang_adapter = ArrayAdapter.createFromResource(
                this, R.array.item_array, R.layout.spinner_layout);

        
                       // Set the Adapter
        spinner.setAdapter(lang_adapter);


      //for the calendar
      		calendar_button = (ImageButton) findViewById(R.id.imageButton1);
      		search_button = (ImageButton) findViewById(R.id.imageButton2);

      		cal = Calendar.getInstance();
      		day = cal.get(Calendar.DAY_OF_MONTH);
      		month = cal.get(Calendar.MONTH);
      		year = cal.get(Calendar.YEAR);
      		et = (EditText) findViewById(R.id.editText);
      		//display current date
      		
      		 SimpleDateFormat dfDate_day= new SimpleDateFormat("dd/MM/yyyy");
      		    String dt="";
      		    Calendar c = Calendar.getInstance(); 
      		    dt=dfDate_day.format(c.getTime());
      		    et.setText(dt);
      	   
      	    
      		
      		  calendar_button.setOnClickListener(this);
      		  search_button.setOnClickListener(this);


        //for the locations
        from_txt = (AutoCompleteTextView) findViewById(R.id.from_txt);
        to_txt = (AutoCompleteTextView) findViewById(R.id.to_txt);

        String country_List[]={""};        
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,country_List);
        
        
        from_txt.setAdapter(adapter);
        to_txt.setAdapter(adapter);

      from_txt.addTextChangedListener(new TextWatcher() {
    	  private String lastValue = "";
          @Override
          public void onTextChanged(CharSequence s, int start, int before,
                  int count) {
              // TODO Auto-generated method stub
             
              
          }
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count,
                  int after) {
                
          }

          @Override
          public void afterTextChanged(Editable s) {
              // TODO Auto-generated method stub

              // TODO Auto-generated method stub
        	    String newValue = from_txt.getText().toString();

        	    if (!newValue.equals(lastValue)) {
        	        lastValue = newValue;
        	        the_caller=1;

	        	theCall(newValue);
        	    }
	        	  from_txt.setSelection(from_txt.getText().length());

          }
          
          
		
      });
      
     to_txt.addTextChangedListener(new TextWatcher() {
    	  private String lastValue = "";

          @Override
          public void onTextChanged(CharSequence s, int start, int before,
                  int count) {
              // TODO Auto-generated method stub
             
              
          }
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count,
                  int after) {
                
          }

          @Override
          public void afterTextChanged(Editable s) {
              // TODO Auto-generated method stub

              // TODO Auto-generated method stub
             

        	    String newValue = to_txt.getText().toString();

        	    if (!newValue.equals(lastValue)) {
        	        lastValue = newValue;
        	        the_caller=2;

	        	theCall(newValue);
        	    }
        	    to_txt.setSelection(to_txt.getText().length());

          }
          
          
		
      });
      
        handler = new Handler();
        progressDialog = new ProgressDialog(this);
        gps = new GPSTracker(this);
     // check if GPS enabled		
        if(gps.canGetLocation()){
        	
        	 current_latitude = gps.getLatitude();
        	 current_longitude = gps.getLongitude();
        	
        	// \n is for new line
        //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + current_latitude + "\nLong: " + current_longitude, Toast.LENGTH_LONG).show();	
        }else{
        	// can't get location
        	// GPS or Network is not enabled
        	// Ask user to enable GPS/network in settings
        	gps.showSettingsAlert();
        }
      }
	
	
     
	@Override
	public void notifyHTTPRespons(final HttpHandler http) {
		// TODO Auto-generated method stub
		Log.i("Log", "responce ==  "+http.getResCode());
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				
				String result = http.getResponse();
				
				try {
					
					JSONObject myResult = new JSONObject(result);
					String results_string = myResult.getString("results");
					results_Array = new JSONArray(results_string);
					
					adapter.clear();
					//calculate distance
					for (int i = 0; i < results_Array.length(); i++) {
					    JSONObject row = results_Array.getJSONObject(i);
					    JSONObject geo_object=row.getJSONObject("geo_position");
						Float latitude=Float.parseFloat(geo_object.getString("latitude"));
						Float longitude=Float.parseFloat(geo_object.getString("longitude"));
					  //calculate distance
					    //create 2 location objects
					    Location loc1 = new Location("");
					loc1.setLatitude(current_latitude);
					loc1.setLongitude(current_longitude);
					Location loc2 = new Location("");
					loc2.setLatitude(latitude);
					loc2.setLongitude(longitude);
					float distanceInMeters = loc1.distanceTo(loc2);
					//insert the distance to every object
					row.put("distance",distanceInMeters);
					
					}
					//sort objects
					JSONArray SortedResult =sortJsonArray(results_Array);
					for (int i = 0; i < SortedResult.length(); i++) {
					    JSONObject row = SortedResult.getJSONObject(i);
					    String name=row.getString("name");
					    adapter.add(name);
					}
					//end sorting
				      adapter.notifyDataSetChanged();
				      if (the_caller==1)
				      from_txt.setText(from_txt.getText());
				      else
				      to_txt.setText(to_txt.getText());

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
	}
	
	
	//sorting JSON array
	public static JSONArray sortJsonArray(JSONArray array) throws JSONException {
	    List<JSONObject> jsons = new ArrayList<JSONObject>();
	    for (int i = 0; i < array.length(); i++) {
	        jsons.add(array.getJSONObject(i));
	    }
	    Collections.sort(jsons, new Comparator<JSONObject>() {
	        @Override
	        public int compare(JSONObject lhs, JSONObject rhs) {
	            String lid="",rid="";
				try {
					lid = lhs.getString("distance"); 
					rid = rhs.getString("distance");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	          
	            return lid.compareTo(rid);
	        }
	    });
	    return new JSONArray(jsons);
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
		
		// TODO Auto-generated method stub
		//Log.i("", msg)

	}
  	public void theCall(final String typed) {
			// TODO Auto-generated method stub
			  new Thread(new Runnable() {
	      			
	      			@Override
	      			
	      			public void run() {
	      				// TODO Auto-generated method stub
	      				String lang = spinner.getSelectedItem().toString();
	      				HttpHandler handler = new HttpHandler("https://api.goeuro.com/api/v2/position/suggest/"+lang+"/"+typed,null, null, 123);
	      				handler.addHttpLisner(HttpConnectionApiActivity.this);
	      				handler.sendRequest();
	      			}
	      		}).start();
			
		}



  	@Override
	public void onClick(View v) {
  		 if(v.getId() == R.id.imageButton1) {
  			showDialog(0);

  	    }else if(v.getId() == R.id.imageButton2) {
  	       Toast.makeText(getApplicationContext(), "Search is not yet implemented", Toast.LENGTH_LONG).show();	
  	    }
	}

	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		return new DatePickerDialog(this, datePickerListener, year, month, day);
	}
	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			String user_date= (selectedDay + "/" + (selectedMonth + 1) + "/"
					+ selectedYear);
			
			 Date userDate = null;
			SimpleDateFormat  format = new SimpleDateFormat("dd/MM/yyyy");  
			try {  
			    userDate = format.parse(user_date);  
			 } catch (Exception e) {  
			    // TODO Auto-generated catch block  
			    e.printStackTrace();  
			}
			
			if (new Date().after(userDate)) 
		  	       Toast.makeText(getApplicationContext(), "please insert a valid date", Toast.LENGTH_LONG).show();
			else
			et.setText(selectedDay + "/" + (selectedMonth + 1) + "/"
					+ selectedYear);
		}
	};
	

}
