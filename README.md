# recyclerview_bug
There is some bug when working with RecyclerView placed as a child in LinearLayout with weight specified. When updating items in adapter the recyclerview scroll to the updated item and prevents user from scrolling.

To produce behaviour just scroll down a little and tap on one of the items. A progress bar should show up and item should be updated.

## Demo working
![](video_working.mp4)

## Demo failing
![](video_failing.mp4)

## Where is the problem?
I've add two layouts for comparison: one working and one failing. You can compare  activity_bug.xml and activity_ok.xml for differences.

```xml
 <android.support.v7.widget.RecyclerView
    android:id="@+id/recycler"
    android:layout_width="0dp"
    android:layout_weight="1"
    android:layout_height="match_parent"
    android:background="@android:color/holo_orange_light"/>
```

vs 

```xml
<android.support.v7.widget.RecyclerView
      android:id="@+id/recycler"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:background="@android:color/holo_orange_light"/>
```