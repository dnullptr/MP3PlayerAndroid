package com.example.mp3player;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Playlist {
   boolean isLooped=true;
   int i,playInd;
   ArrayList<String> fullPathList;
   Map<Integer,String[]> title_artist_Map;

   public Playlist()
   {
       fullPathList=new ArrayList<>();
       title_artist_Map=new HashMap<>();
       i=0;playInd=0;
   }
   public void addSong(String fullPath,String title,String artist)
   {
       fullPathList.add(fullPath);
       title_artist_Map.put(i,new String[]{title,artist});
       i++;
       Log.d("PlaylistClass", "addedSong! ind: "+i+" is : "+fullPath);
   }

   public void setFullPathList(ArrayList<String> param) //risky, unaware of indexes, but possible.
   {
       this.fullPathList=param;
   }

   public String getTitleWithIndex(int i)
   {
       return Objects.requireNonNull(this.title_artist_Map.get(i))[0];
   }
    public String getArtistWithIndex(int i)
    {
        return Objects.requireNonNull(this.title_artist_Map.get(i))[1];
    }

    public String getFormattedTitleArtsist(int i) {
        return Objects.requireNonNull(this.title_artist_Map.get(i))[0]+" - "+Objects.requireNonNull(this.title_artist_Map.get(i))[1];
    }

    public String getFullPathOfIndex(int i)
    {
        return this.fullPathList.get(i);
    }

    public String getCurrentFullPath()
    {
        return this.fullPathList.get(playInd);
    }
    public void moveToFirst()
    {
        playInd=0;
    }
    public void moveToLast()
    {
        playInd=i;
    }

    public void setLooped(boolean looped) {
        isLooped = looped;
    }
    public boolean isLooped()
    {
        return isLooped;
    }

    public String moveToAndGetNext()
    {
        if(playInd < fullPathList.size()-1)
            return this.fullPathList.get(++playInd);
        else if (playInd == fullPathList.size()-1 && isLooped)
        {
            moveToFirst();
            return this.fullPathList.get(++playInd);
        }
        else return this.fullPathList.get(playInd); //wont move

    }
    public String moveToAndGetPrev()
    {
        if(playInd > 0){
            Log.d("PREVCLASS", "moveToAndGetPrev: "+playInd);
            return this.fullPathList.get(--playInd);
        }

        else if (playInd == 0 && isLooped)
        {
            moveToLast();
            Log.d("PREVCLASS", "moveToAndGetPrev: "+playInd);
            return this.fullPathList.get(--playInd);
        }
        else return this.fullPathList.get(playInd); //wont move

    }
}
