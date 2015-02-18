<h1>UCSD-Shuttle-App-Widget</h1>
An Android application with a widget to display shuttle stop arrival times for UCSD campus shuttles

<h3>Purpose</h3>
to have a widget on the homescreen of your android device that automatically shows what time a shuttle shows up. The user can configure times and days of the week to associate with a shuttle. During that time, the widget will show how long till the bus arrives.

<h3>Basic Info</h3>

| Class Name          |Info                                                                                                  |
|---------------------|------------------------------------------------------------------------------------------------------|
|StopSchedulerActivity|The primary activiy. It holds a ViewPager and an instance of ConstraintDatabase. The ViewPager shows 3 Views (Lookup View, Week View, and List View) that describe shuttle and widget information.|
|ConstraintDatabase|A SQLiteOpenHelper that handles opening a SQLite database. This database holds the widget schedule information, and has several methods to manipulate it|
|ShuttleConstants|A class static holding data that the whole app may need.|
|StopSchedulerService|An IntentService that handles what information a given shuttle has to offer. This is asynchronous because it needs to access the internet, and is a service because it must be able to update the AppWidget.|
|ShuttleWidgetProvider|A class that handles the AppWidget, it receives intents from StopSchedulerService and displays updates|
It is recommended you read this application starting with StopSchedulerActivity. Also make sure to check out ShuttleWidgetProvider.


<h3>Build</h3>
This is made using Gradle in Android Studio. The repository only holds the res and src directories, plus AndroidManifest.xml. You can pull and compile the project yourself or use the supplied apk.
