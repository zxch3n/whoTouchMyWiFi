
# Wi-Fi IP Scaning Report

There is a Wi-Fi in the hotel I live, which is used by over 50 devices. Sometime I use that, I may be kicked out for too many connctions to this Wi-Fi. It occurs to me that I can collect these connections information to analyse daily schedules of people who live here. And I can find a way to also avoid using this Wi-Fi at the peak of connections' number.

This data is collected by an Android app.



```python
import numpy as np
import pandas as pd
import seaborn as sns
import time
from IPython.display import display, Markdown
import matplotlib.pyplot as plt

%matplotlib inline
```

Read data from the file.


```python
def read(path):
    data = []
    with open(path) as f:
        s = f.readline()
        while s:
            s = s[:-1]
            data.append(s.split(' '))
            s = f.readline()

    df = pd.DataFrame(data, columns=['ip', 'mac', 'name', 'time'])
    df['hour'] = [time.gmtime(int(t)).tm_hour for t in df.time]
    df['minute'] = [time.gmtime(int(t)).tm_min for t in df.time]
    df['wday'] = [time.gmtime(int(t)).tm_wday for t in df.time]
    df['day_hour'] = df.hour + df.minute/60
    df['week_day'] = df.wday + df.day_hour/24
    return df

raw_df = read('./wifi_status.txt')
```

The format of this data is like the table following. 



Here is the columns:
- time. It is the time when this application found that connection. 
- name. It should be the name of that device. Here is a problem in that data.
- ip. Devices' IP addresses, assigned by the route. Most of it is useless for this report.
- mac. The MAC addresses of each devices, which are determined when birth, and should be unique in earth. Play key role in this report.


```python
raw_df.head()
```




<div>
<style>
    .dataframe thead tr:only-child th {
        text-align: right;
    }

    .dataframe thead th {
        text-align: left;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>ip</th>
      <th>mac</th>
      <th>name</th>
      <th>time</th>
      <th>hour</th>
      <th>minute</th>
      <th>wday</th>
      <th>day_hour</th>
      <th>week_day</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>192.168.101.1</td>
      <td>00:90:27:e0:d9:30</td>
      <td>localhost</td>
      <td>1505629299</td>
      <td>6</td>
      <td>21</td>
      <td>6</td>
      <td>6.35</td>
      <td>6.264583</td>
    </tr>
    <tr>
      <th>1</th>
      <td>192.168.101.3</td>
      <td>a8:66:7f:34:3a:3c</td>
      <td>localhost</td>
      <td>1505629299</td>
      <td>6</td>
      <td>21</td>
      <td>6</td>
      <td>6.35</td>
      <td>6.264583</td>
    </tr>
    <tr>
      <th>2</th>
      <td>192.168.101.10</td>
      <td>2c:1f:23:2d:ed:ef</td>
      <td>localhost</td>
      <td>1505629299</td>
      <td>6</td>
      <td>21</td>
      <td>6</td>
      <td>6.35</td>
      <td>6.264583</td>
    </tr>
    <tr>
      <th>3</th>
      <td>192.168.101.16</td>
      <td>80:13:82:d8:8b:71</td>
      <td>localhost</td>
      <td>1505629299</td>
      <td>6</td>
      <td>21</td>
      <td>6</td>
      <td>6.35</td>
      <td>6.264583</td>
    </tr>
    <tr>
      <th>4</th>
      <td>192.168.101.18</td>
      <td>c4:d9:87:f4:70:90</td>
      <td>localhost</td>
      <td>1505629299</td>
      <td>6</td>
      <td>21</td>
      <td>6</td>
      <td>6.35</td>
      <td>6.264583</td>
    </tr>
  </tbody>
</table>
</div>



Get the connections num by the raw data. And add diff layers of time data BTW.


```python
def get_df_connections_time(raw_df):
    df = raw_df.groupby('time').count()
    df = pd.DataFrame({'time':df.index, 'n_connections':df.ip})
    df['hour'] = [time.gmtime(int(t)).tm_hour for t in df.time]
    df['minute'] = [time.gmtime(int(t)).tm_min for t in df.time]
    df['wday'] = [time.gmtime(int(t)).tm_wday for t in df.time]
    df['day_hour'] = df.hour + df.minute/60
    df['week_day'] = df.wday + df.day_hour/24
    # remove the error part (connections number == 1, suggest that this device just get kicked out)
    df = df.where(df.n_connections > 2).dropna()
    return df

df_ct = get_df_connections_time(raw_df)
display(df_ct.head())

```


<div>
<style>
    .dataframe thead tr:only-child th {
        text-align: right;
    }

    .dataframe thead th {
        text-align: left;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>n_connections</th>
      <th>time</th>
      <th>hour</th>
      <th>minute</th>
      <th>wday</th>
      <th>day_hour</th>
      <th>week_day</th>
    </tr>
    <tr>
      <th>time</th>
      <th></th>
      <th></th>
      <th></th>
      <th></th>
      <th></th>
      <th></th>
      <th></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>1505629299</th>
      <td>32.0</td>
      <td>1505629299</td>
      <td>6.0</td>
      <td>21.0</td>
      <td>6.0</td>
      <td>6.350000</td>
      <td>6.264583</td>
    </tr>
    <tr>
      <th>1505629320</th>
      <td>16.0</td>
      <td>1505629320</td>
      <td>6.0</td>
      <td>22.0</td>
      <td>6.0</td>
      <td>6.366667</td>
      <td>6.265278</td>
    </tr>
    <tr>
      <th>1505629339</th>
      <td>27.0</td>
      <td>1505629339</td>
      <td>6.0</td>
      <td>22.0</td>
      <td>6.0</td>
      <td>6.366667</td>
      <td>6.265278</td>
    </tr>
    <tr>
      <th>1505630166</th>
      <td>28.0</td>
      <td>1505630166</td>
      <td>6.0</td>
      <td>36.0</td>
      <td>6.0</td>
      <td>6.600000</td>
      <td>6.275000</td>
    </tr>
    <tr>
      <th>1505630408</th>
      <td>27.0</td>
      <td>1505630408</td>
      <td>6.0</td>
      <td>40.0</td>
      <td>6.0</td>
      <td>6.666667</td>
      <td>6.277778</td>
    </tr>
  </tbody>
</table>
</div>


# Data Visualize

## Data Density



```python
def plot_hour_density(df, name='', ax=None):
    if ax is None:
        fig = plt.figure(figsize=(12, 6))
    plt.title('Hour Density' + name)
    plt.xlim((0, 24))
    if ax is None:
        plt.xticks([i for i in range(24)])
    plt.xlabel('Hour')
    plt.ylabel('Num')
    if ax is None:
        ax = plt.subplot(111)
    sns.distplot(df.day_hour, ax=ax, bins=24, kde=False)

def plot_weekday_density(df):
    temp = df.groupby('wday').count().hour
    fig = plt.figure(figsize=(12, 6))
    plt.title('Week Day Density')
    plt.xticks([i for i in range(7)])
    plt.xlabel('Week Day')
    plt.ylabel('Data Num')
    ax = plt.subplot(111)
    plt.bar(list(range(7)), temp.values)
    
plot_hour_density(df_ct)
plot_weekday_density(df_ct)
plt.figure(figsize=(14, 10))
for i in range(7):
    ax = plt.subplot(2, 4, i + 1)
    df = df_ct.loc[(df_ct.week_day // 1) == i]
    plot_hour_density(df, name="on Week Day " + str(i), ax=ax)
```


![png](output_9_0.png)



![png](output_9_1.png)



![png](output_9_2.png)


## Daily Change


The plot below shows average trend of the number of connections during a day in the data. As you can see below, the number of connections reach peak at 5pm, and most of them disconnected at 3am to 7am. And 8am is an interesting point, when a part of people wake up and go to work, and in 8:30 the number reachs the bottom again, which makes sense.

However, here comes the questions:

1. Why should it reach peak at 17:00? 
2. Why does it increace from 9am till 5pm?
3. Why does it fall down rapidly from 5pm to 6pm? 
4. Who are the dozen of people that leave during 5pm to 6pm? When do they connect to the WiFi?
5. Who are the dozen of people that leave during 7pm to 9pm? What do their daily schedules look like?
6. Who are the people sleep very late? What's their daily schedules look like?
7. Which devices conneted to the Wi-Fi all the time?


```python
plt.figure(figsize=(16, 8))
plt.title('Daily Trend of Connections Number')
plt.ylabel('connections number')
plt.ylim((0, 70))
plt.xticks([i for i in range(24)])
conn_day_change = df_ct.groupby('day_hour').mean().n_connections
day_trend = conn_day_change.rolling(10).mean()
day_trend.plot()

```




    <matplotlib.axes._subplots.AxesSubplot at 0x1bd59ccb518>




![png](output_11_1.png)


## Weekly Trend

This is the plot to demonstrate the connections number change during a week. Obviously this should be sample at the same time windows in a day. Since the data I collected here is not continuous, plotting directly make no sense. 

It seems like a good way to substract the average daily trend from the data of every week day. In this way, we can remove the daily cycle of the data.

With these, I can solve those questions:

1. Who go out at weekend?
2. Who come here at weekend?
3. Are there ones who stay here all the week? (except for the long last connected ones)

### Raw Plot

This is the directly plot of everyday connections number change. As you can see, this plot does not make much sense. Because there are daily change in it which affect the result.


```python
plt.figure(figsize=(16, 8))
plt.ylabel('connections number')
plt.ylim((0, 70))
plt.xticks([i for i in range(7)])
plt.title('Raw Week Trend Data')
weekday_trend = df_ct.groupby('week_day').mean().n_connections
weekday_trend.plot()


```




    <matplotlib.axes._subplots.AxesSubplot at 0x1bd5a086e48>




![png](output_13_1.png)


### Remove the Daily Trend from the Weekly Trend

The results are showing below. The first plot is the daily trend in one week day substract the average daily trend. Because the data is not enough, you might see a lot of gaps in there.

The second plot is the average change of connections number on one day in a week. You can see the change is so small. That's because the data I collect is not enough, the on one day of a week will be very similar to the average daily trend at the corresponding time. If I collect more data, this reasonable weekly trend will make more sense.

#### Time Granularity = 1 min


```python
plt.figure(figsize=(14, 7))
plt.title('Day Diff from Average')
plt.ylabel('Connections Diff')
plt.xticks(list(range(24)))
week_day_mean = []
for i in range(7):
    df = df_ct.loc[df_ct.wday == i]
    df.index = df.day_hour
    day_diff = df.n_connections - day_trend
    week_day_mean.append(day_diff.mean())
    day_diff.plot()
plt.legend(list(range(7)))

plt.figure(figsize=(14, 7))
plt.title('Week Trend')
plt.ylabel('Day Diff')
plt.ylim((-10, 10))
plt.bar(list(range(7)), week_day_mean)
```




    <Container object of 7 artists>




![png](output_15_1.png)



![png](output_15_2.png)


#### Time Granularity = 1 hour

A bigger size of granularity would reduce the need of data here, but it may be a little more untrustworthy.

The result is shown as below


```python
day_hour_trend = df_ct.groupby('hour').mean().n_connections
plt.figure(figsize=(14, 7))
plt.title('Day Diff from Average (Hour Level)')
plt.ylabel('Connections Diff')
plt.xticks(list(range(24)))
plt.ylim((-40, 40))
week_day_mean = []
for i in range(7):
    df = df_ct.loc[df_ct.wday == i]
    df = df.groupby('hour').mean().n_connections
    day_diff = df - day_hour_trend
    week_day_mean.append(day_diff.mean())
    day_diff.plot()
plt.legend(list(range(7)))

plt.figure(figsize=(14, 7))
plt.title('Week Trend (Hour Level)')
plt.ylabel('Day Diff')
plt.ylim((-10, 10))
plt.bar(list(range(7)), week_day_mean)
```




    <Container object of 7 artists>




![png](output_17_1.png)



![png](output_17_2.png)


# Indivisual Tracking

The analysis in this section aim to solve the questions described above.

#### Who are the dozen of people that leave during 5pm to 6pm? When do they connect to the WiFi?

1. Find out who disconnected and connected to the wifi at a time point
2. Create a method get_connecte_devices(start_time, end_time) & dis
3. Create a method get_connect_time(MACs) & dis


```python

```
