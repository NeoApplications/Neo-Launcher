package com.saggitt.omega.preferences;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.AppInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.util.ComponentKey;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;
import static com.android.launcher3.util.Executors.MODEL_EXECUTOR;

public class IconSizeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<View> mItemViews;
    private LayoutInflater mInflater;
    private int TYPE_LOADING = 0;
    private int TYPE_ITEM = 1;
    boolean isLoaded = false;
    ArrayList<App> apps = new ArrayList<>();

    public IconSizeAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;

        postLoadApps();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        Log.d("IconSizeAdapter", "Cargando vistas");
        if (getItemViewType(0) == TYPE_ITEM) {
            return new AppHolder(mInflater.inflate(R.layout.all_apps_icon, parent, false));
        } else {
            return new LoadingHolder(mInflater.inflate(R.layout.adapter_loading, parent, false));
        }
    }

    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private void postLoadApps() {
        MODEL_EXECUTOR.getHandler().postAtFrontOfQueue(this::loadAppsList);
    }

    protected void loadAppsList() {
        List<LauncherActivityInfo> activityList = getAppsList(mContext);
        for (LauncherActivityInfo app : activityList) {
            apps.add(new App(mContext, app));
        }
        MAIN_EXECUTOR.getHandler().postAtFrontOfQueue(this::onAppsListLoaded);
    }

    public int getItemViewType(int position) {
        return isLoaded ? TYPE_ITEM : TYPE_LOADING;
    }

    protected void onAppsListLoaded() {
        isLoaded = true;
        notifyDataSetChanged();
    }

    private ArrayList<LauncherActivityInfo> getAppsList(Context context) {
        ArrayList<LauncherActivityInfo> activities = new ArrayList<>();
        List<UserHandle> profiles = UserManagerCompat.getInstance(context).getUserProfiles();
        LauncherAppsCompat launcherAppsCompat = LauncherAppsCompat.getInstance(context);

        for (UserHandle profile : profiles)
            activities.addAll(launcherAppsCompat.getActivityList(null, profile));

        if (activities.size() > 4)
            activities.remove(activities.size() - 1);

        return activities;
    }

    public static class App {
        Drawable iconDrawable;
        ComponentKey key;

        public App(Context context, LauncherActivityInfo info) {
            key = new ComponentKey(info.getComponentName(), info.getUser());
            AppInfo appInfo = new AppInfo(context, info, info.getUser());
            LauncherAppState.getInstance(context).getIconCache().getTitleAndIcon(appInfo, false);
            iconDrawable = new BitmapDrawable(context.getResources(), appInfo.iconBitmap);
        }
    }

    class AppHolder extends RecyclerView.ViewHolder {
        private ImageView icon;

        public AppHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
        }

        public void bind(int position) {
            App app = apps.get(position);
            icon.setImageDrawable(app.iconDrawable);
        }

    }

    public static class LoadingHolder extends RecyclerView.ViewHolder {
        public LoadingHolder(View itemView) {
            super(itemView);
        }
    }

    /*private int TYPE_LOADING = 0;
    private int TYPE_ITEM = 1;

    boolean isLoaded = false;
    ArrayList<App> apps = new ArrayList<>();

    public IconSizeAdapter(Context context) {
        mContext = context;
        //postLoadApps();

        ArrayList<LauncherActivityInfo> a = getAppsList(mContext);

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        new AppHolder(layoutInflater.inflate(R.layout.all_apps_icon, null, false));
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType){

        Log.d("IconSizeAdapter","Cargando vistas");
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        if (getItemViewType(0) == TYPE_ITEM) {
            return new AppHolder(layoutInflater.inflate(R.layout.all_apps_icon, parent, false));
        } else {
            return new LoadingHolder(layoutInflater.inflate(R.layout.adapter_loading, parent, false));
        }
    }

    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AppHolder) {
            ((AppHolder) holder).bind(position);
        }
    }

    public int getItemViewType(int position){
        return isLoaded ? TYPE_ITEM : TYPE_LOADING;
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public int getCount(){
        return 0;
    }

    private void postLoadApps() {
        MODEL_EXECUTOR.getHandler().postAtFrontOfQueue(this::loadAppsList);
    }

    protected void loadAppsList() {
        List<LauncherActivityInfo> activityList  = getAppsList(mContext);
        for(LauncherActivityInfo app : activityList){
            apps.add(new App(mContext, app));
        }
        MAIN_EXECUTOR.getHandler().postAtFrontOfQueue(this::onAppsListLoaded);
    }

    protected void onAppsListLoaded() {
        isLoaded = true;
        notifyDataSetChanged();
    }

    private ArrayList<LauncherActivityInfo> getAppsList(Context context) {
        ArrayList<LauncherActivityInfo> activities = new ArrayList<>();
        List<UserHandle> profiles = UserManagerCompat.getInstance(context).getUserProfiles();
        LauncherAppsCompat launcherAppsCompat = LauncherAppsCompat.getInstance(context);

        for (UserHandle profile : profiles)
            activities.addAll(launcherAppsCompat.getActivityList(null, profile));

        if(activities.size() > 4)
            activities.remove(activities.size()-1);

        return activities;
    }


    public static class App{
        Drawable iconDrawable;
        ComponentKey key ;

        public App(Context context, LauncherActivityInfo info){
            key = new ComponentKey(info.getComponentName(), info.getUser());
            AppInfo appInfo = new AppInfo(context, info, info.getUser());
            LauncherAppState.getInstance(context).getIconCache().getTitleAndIcon(appInfo, false);
            iconDrawable = new BitmapDrawable(context.getResources(), appInfo.iconBitmap);
        }
    }


    class AppHolder extends RecyclerView.ViewHolder{
        private ImageView icon;
        public AppHolder(View itemView){
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
        }

        public void bind(int position) {
            App app = apps.get(position);
            icon.setImageDrawable(app.iconDrawable);
        }

    }

    public static class LoadingHolder extends RecyclerView.ViewHolder{
        public LoadingHolder (View itemView){
            super(itemView);
        }
    }
*/
}
