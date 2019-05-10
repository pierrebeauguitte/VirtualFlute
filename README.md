# Activities
I have created 4 activities, including [intro page](https://github.com/pierrebeauguitte/VirtualFlute/blob/master/app/src/main/res/layout/activity_intro.xml), [virtual flute](https://github.com/pierrebeauguitte/VirtualFlute/blob/master/app/src/main/res/layout/test.xml), [searching page](https://github.com/pierrebeauguitte/VirtualFlute/blob/master/app/src/main/res/layout/activity_test_search.xml) and [result page](https://github.com/pierrebeauguitte/VirtualFlute/blob/master/app/src/main/res/layout/activity_test_result.xml).

The related java files are here: [intro page](https://github.com/pierrebeauguitte/VirtualFlute/blob/master/app/src/main/java/com/dit/pierre/virtualflute/Intro.java), [virtual flute](https://github.com/pierrebeauguitte/VirtualFlute/blob/master/app/src/main/java/com/dit/pierre/virtualflute/Test.java), [searching page](https://github.com/pierrebeauguitte/VirtualFlute/blob/master/app/src/main/java/com/dit/pierre/virtualflute/TestSearch.java) and [result page](https://github.com/pierrebeauguitte/VirtualFlute/blob/master/app/src/main/java/com/dit/pierre/virtualflute/TestResult.java)

All the xml files were newly created and java code were mostly copied from Pierre.
## Intro Page
On the intro page, there is an image button (a record button image from google) that is going to direct to the next activity, virtual flute.

```xml
<ImageButton
  android:id="@+id/bt_record"
  android:layout_width="wrap_content"
  android:layout_height="wrap_content"
  android:layout_centerInParent="true"
  android:background="@drawable/record" />
```
```java
ImageButton btRecord = (ImageButton) findViewById(R.id.bt_record);
        btRecord.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent myIntent = new Intent(view.getContext(), Test.class);
                startActivityForResult(myIntent, 0);
            }
        });
```
The xml and java file of this activity were entirely created on myself.

## Virtual Flute
In Virtual Flute activity, I have built it by using vertical linear layout, which has a text view at the top, a horizontal linear layout with flute buttons in the middle and a progress bar at the bottom.

The hardest part to build in this activity was to make round button. After some research, I have found out a way of making it, which was to make an [extra xml file](https://github.com/pierrebeauguitte/VirtualFlute/blob/master/app/src/main/res/layout/sample_tbutton.xml) just for round button.
```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res/com.example.pierre.customtouch"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.dit.pierre.virtualflute.TButton
        android:background="#ccc"
     />
</FrameLayout>
```

Apart from xml files, I have also changed the code in Java file so that the recording process will start automatically as soon as the activity is created.

## Search
On Search, there is a textview and a progress bar which shows the searching progress. This activity will be created after finishing the recording of previous activity, Virtual Flute.

Apart from the layout, I have changed the way of displaying the result, which passes the necessary information to the next activity, the result page.

In the AsyncTask, I have called an extra method in the activity class, which picks up a lists of tunes, pre-process the data and pass it to the next activity via intent.
```java
//onPostExecute from AsyncTask
protected void onPostExecute(List<Tune> cumulResults) {
    this.testSearch.returnThreadResult(cumulResults);
}

//method in main activity, which calls the next activity
public void returnThreadResult(List<Tune> result){
  int[] _id = new int[result.size()];
  int[] setting = new int[result.size()];
  String[] name = new String[result.size()];
  double[] score = new double[result.size()];
  for(int i = 0; i < result.size(); i++){
    Tune tune = result.get(i);
    _id[i] = tune.get_id();
    setting[i] = tune.getSetting();
    name[i] = tune.getName();
    score[i] = tune.getScore();
    System.out.println(tune.toString());
  }
  Intent intent = new Intent(this, TestResult.class);
  intent.putExtra("_id", _id);
  intent.putExtra("setting", setting);
  intent.putExtra("name", name);
  intent.putExtra("score", score);
  startActivity(intent);
}
```

## Result
This activity takes in the result information from previous activity and display the result in a listview, which all of them are clickable. After clicking the buttons on listview, the website of that song will be opened in the default browser.
```java
//create clickable listview in onCreate()
for(int i = 0; i < _id.length; i++){
   String temp = String.format(Locale.ENGLISH,
   "[%d_%d] %s - %.2f",
  _id[i], setting[i], name[i], score[i]* 100);
  results.add(temp);
}
ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, results);
lvResult.setAdapter(adapter);
lvResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    String uri = "https://thesession.org/tunes/" + _id[position] + "#setting" + setting[position];
    Intent uriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    startActivity(uriIntent);
  }
});
```
Both xml and java file were created by myself.
