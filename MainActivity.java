package com.pocketwar.game;

import android.app.*;
import android.os.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.content.*;
import java.util.*;

public class MainActivity extends Activity {
  GameView game;
  @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setFlags(1024,1024); game=new GameView(this); setContentView(game);}
}

class GameView extends View {
  Paint p=new Paint(3); Paint text=new Paint(3); int screen=0, map=0, turn=1; boolean blueTurn=true;
  ArrayList<Unit> units=new ArrayList<>(); Unit selected=null; int mode=0; //0 select 1 move 2 attack
  float sx,sy; Random rnd=new Random(4);
  int[] blueX={-3,-2,-3}, blueZ={-1,-2,1}, redX={3,2,3}, redZ={1,2,-1};
  GameView(Context c){super(c); text.setTypeface(Typeface.create("sans",Typeface.BOLD)); setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
  protected void onDraw(Canvas c){super.onDraw(c); if(screen==0)menu(c); else if(screen==1)maps(c); else battle(c);}
  void bg(Canvas c,int color){c.drawColor(color);}
  void button(Canvas c,String s,float l,float t,float r,float b,int col){p.setColor(col);p.setStyle(Paint.Style.FILL);c.drawRoundRect(l,t,r,b,18,18,p);p.setColor(Color.WHITE);p.setTextSize(Math.min(34,(b-t)*.34f));p.setTextAlign(Paint.Align.CENTER);c.drawText(s,(l+r)/2,t+(b-t)*.62f,text);}
  void menu(Canvas c){bg(c,Color.rgb(5,17,29));text.setTextAlign(Paint.Align.CENTER);text.setTextSize(74);text.setColor(Color.rgb(255,181,45));c.drawText("POCKET WAR",getWidth()/2,125,text);text.setTextSize(24);text.setColor(Color.LTGRAY);c.drawText("TACTICS • BATTLE • VICTORY",getWidth()/2,160,text);
    float l=getWidth()/2-230,r=getWidth()/2+230;button(c,"⚔ PLAY VS AI",l,205,r,270,Color.rgb(20,150,67));button(c,"🌐 ONLINE MULTIPLAYER",l,285,r,350,Color.rgb(35,95,205));button(c,"⚙ SETTINGS",l,365,r,430,Color.rgb(25,70,105));
    text.setTextSize(18);text.setColor(Color.rgb(140,170,190));c.drawText("Original 3D/isometric strategy prototype",getWidth()/2,475,text);
  }
  void maps(Canvas c){bg(c,Color.rgb(6,18,29));text.setTextAlign(Paint.Align.CENTER);text.setTextSize(38);text.setColor(Color.WHITE);c.drawText("SELECT MAP",getWidth()/2,70,text);
    String[] n={"GREEN FIELDS","DESERT BASE","ISLAND WAR","MOUNTAIN PASS"}; int[] cols={0xff4d9343,0xffb68a4f,0xff167aa1,0xff687a83};
    for(int i=0;i<4;i++){float l=50+(i%2)*getWidth()/2f,t=105+(i/2)*180,r=getWidth()/2-20+(i%2)*getWidth()/2f,b=t+145;p.setColor(cols[i]);c.drawRoundRect(l,t,r,b,18,18,p);text.setTextSize(25);text.setColor(Color.WHITE);c.drawText(n[i],(l+r)/2,b-25,text);}
    button(c,"← MAIN MENU",getWidth()/2-170,485,getWidth()/2+170,545,0xff16405d);
  }
  void build(){units.clear();String[] types={"INFANTRY","TANK","ARTILLERY"};for(int i=0;i<3;i++)units.add(new Unit(true,types[i],blueX[i],blueZ[i]));for(int i=0;i<3;i++)units.add(new Unit(false,types[i],redX[i],redZ[i]));}
  float isoX(float x,float z){return getWidth()/2+(x-z)*42;}
  float isoY(float x,float z){return 205+(x+z)*21;}
  void battle(Canvas c){bg(c,0xff83c9ea); // terrain
    for(int z=-4;z<=4;z++)for(int x=-5;x<=5;x++){float X=isoX(x,z),Y=isoY(x,z);p.setColor((x+z)%3==0?0xff3f823d:0xff559948);Path q=new Path();q.moveTo(X,Y);q.lineTo(X+42,Y+21);q.lineTo(X,Y+42);q.lineTo(X-42,Y+21);q.close();c.drawPath(q,p);}
    // river
    p.setColor(0xff167aa1);Path river=new Path();river.moveTo(0,300);river.lineTo(getWidth(),255);river.lineTo(getWidth(),390);river.lineTo(0,435);river.close();c.drawPath(river,p);
    // bridge
    p.setColor(0xff8b5b35);c.drawRect(getWidth()/2-65,270,getWidth()/2+65,315,p);
    // trees/rocks
    for(int i=0;i<22;i++){int x=(i*3)%9-4,z=(i*5)%7-3;float X=isoX(x,z),Y=isoY(x,z)-18;p.setColor(0xff744b2c);c.drawRect(X-5,Y,X+5,Y+28,p);p.setColor(0xff23663a);c.drawCircle(X,Y-10,22,p);}
    for(Unit u:units)drawUnit(c,u);
    p.setColor(0xee061727);c.drawRect(0,0,getWidth(),72,p);text.setTextAlign(Paint.Align.LEFT);text.setTextSize(25);text.setColor(Color.WHITE);c.drawText("⭐ Turn "+turn+" • "+(blueTurn?"YOUR MOVE":"ENEMY MOVE"),20,45,text);
    text.setTextAlign(Paint.Align.RIGHT);c.drawText("Objective: Destroy enemy HQ",getWidth()-20,45,text);
    p.setColor(0xee061727);c.drawRect(0,getHeight()-92,getWidth(),getHeight(),p);
    text.setTextAlign(Paint.Align.LEFT);text.setTextSize(18);text.setColor(Color.WHITE);c.drawText(selected==null?"Tap a blue unit":selected.type+"  HP "+selected.hp+"/"+selected.max+"  Move "+selected.move+"  Range "+selected.range,20,getHeight()-55,text);
    button(c,"MOVE",getWidth()-430,getHeight()-78,getWidth()-325,getHeight()-18,0xffd59a19);button(c,"ATTACK",getWidth()-310,getHeight()-78,getWidth()-205,getHeight()-18,0xff235a88);button(c,"END TURN",getWidth()-190,getHeight()-78,getWidth()-20,getHeight()-18,0xff1c7138);
  }
  void drawUnit(Canvas c,Unit u){float X=isoX(u.x,u.z),Y=isoY(u.x,u.z);p.setColor(u.blue?0xff1769c2:0xffc52f38);c.drawRoundRect(X-24,Y-32,X+24,Y+8,10,10,p);p.setColor(0xffe5b58b);c.drawCircle(X,Y-43,12,p);p.setColor(u.blue?0xff173b78:0xff741f25);c.drawCircle(X,Y-49,14,p);p.setColor(Color.WHITE);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(10);c.drawText(u.type.substring(0,1),X,Y+1,text);p.setColor(Color.DKGRAY);c.drawRect(X-25,Y-62,X+25,Y-57,p);p.setColor(0xff45e06b);c.drawRect(X-25,Y-62,X-25+50*(u.hp/(float)u.max),Y-57,p);if(u==selected){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(0xffffe34f);c.drawCircle(X,Y-22,38,p);p.setStyle(Paint.Style.FILL);}}
  public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()!=1)return true;float x=e.getX(),y=e.getY();
    if(screen==0){if(y>190&&y<290){screen=1;invalidate();}return true;}
    if(screen==1){if(y>100&&y<470){int col=x<getWidth()/2?0:1,row=y<290?0:1;map=row*2+col;screen=2;build();invalidate();}else if(y>470){screen=0;invalidate();}return true;}
    if(screen==2){if(y>getHeight()-105&&x>getWidth()-210){endTurn();invalidate();return true;}
      if(y>getHeight()-105&&x>getWidth()-330&&x<getWidth()-205){mode=2;return true;}
      if(y>getHeight()-105&&x>getWidth()-450&&x<getWidth()-325){mode=1;return true;}
      Unit hit=null;float best=55;for(Unit u:units){float d=Math.hypot(x-isoX(u.x,u.z),y-isoY(u.x,u.z));if(d<best){best=(float)d;hit=u;}}
      if(hit!=null){if(mode==2&&selected!=null&&hit.blue!=selected.blue){hit.hp-=selected.damage;if(hit.hp<=0)units.remove(hit);mode=0;}else if(hit.blue&&blueTurn){selected=hit;mode=0;}invalidate();return true;}
      if(mode==1&&selected!=null){int gx=Math.round((x-getWidth()/2)/42f),gz=Math.round((y-205)/21f);selected.x=gx;selected.z=gz;mode=0;invalidate();}return true;
  }
  void endTurn(){selected=null;blueTurn=false;turn++; // simple AI attack
    for(Unit r:new ArrayList<>(units))if(!r.blue){Unit target=null;float bd=999;for(Unit b:units)if(b.blue){float d=Math.abs(r.x-b.x)+Math.abs(r.z-b.z);if(d<bd){bd=d;target=b;}}if(target!=null&&bd<=r.range){target.hp-=r.damage;if(target.hp<=0)units.remove(target);}}
    blueTurn=true;
  }
}
class Unit{
 boolean blue;String type;int x,z,hp,max,move,range,damage;
 Unit(boolean b,String t,int xx,int zz){blue=b;type=t;x=xx;z=zz;if(t.equals("INFANTRY")){max=hp=100;move=3;range=2;damage=25;}else if(t.equals("TANK")){max=hp=180;move=3;range=2;damage=45;}else{max=hp=100;move=2;range=5;damage=50;}}
}