package museummm;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MuseumProj
  extends JFrame
{
  protected Thread museumControl;
  protected Thread westExit;
  protected Thread eastEntrance;
  private DisplayCanvas museumDisplay;
  private DisplayCanvas westDisplay;
  private DisplayCanvas eastDisplay;
  private JButton openButton;
  private JButton closeButton;
  
  public void init()
  {
    setTitle("CIS 461 Multi-Threaded Program: Museum");
    setDefaultCloseOperation(3);
    
    JPanel canvasPanel = new JPanel();
    this.museumDisplay = new DisplayCanvas("Museum", Color.cyan);
    this.westDisplay = new DisplayCanvas("Exit", Color.green);
    this.eastDisplay = new DisplayCanvas("Entrance", Color.green);
    this.museumDisplay.setSize(150, 100);
    this.westDisplay.setSize(150, 100);
    this.eastDisplay.setSize(150, 100);
    canvasPanel.setLayout(new FlowLayout());
    canvasPanel.add(this.westDisplay);
    canvasPanel.add(this.museumDisplay);
    canvasPanel.add(this.eastDisplay);
    
    this.westDisplay.setDisplayType(1);
    this.museumDisplay.setDisplayType(2);
    this.eastDisplay.setDisplayType(3);
    
    JLabel director = new JLabel(" Director: ");
    
    this.openButton = new JButton("Open Museum");
    this.openButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Control.open = true;
        MuseumProj.this.museumDisplay.openWestDoor();
        MuseumProj.this.museumDisplay.openEastDoor();
        MuseumProj.this.eastDisplay.arrive(-1);
      }
    });
    this.closeButton = new JButton("Close Museum");
    this.closeButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Control.open = false;
        MuseumProj.this.museumDisplay.closeEastDoor();
      }
    });
    JPanel directorPanel = new JPanel();
    directorPanel.add(director);
    directorPanel.add(this.openButton);
    directorPanel.add(this.closeButton);
    
    getContentPane().add(canvasPanel, "Center");
    getContentPane().add(directorPanel, "South");
    
    pack();
    setVisible(true);
  }
  
  public DisplayCanvas getEastDisplay()
  {
    return this.eastDisplay;
  }
  
  public DisplayCanvas getWestDisplay()
  {
    return this.westDisplay;
  }
  
  public DisplayCanvas getMuseumDisplay()
  {
    return this.museumDisplay;
  }
  
  public void simulateArrival()
  {
    int delay = (int)(Math.random() * 1000.0D);
    try
    {
      Thread.sleep(delay);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    for (int j = 0; j < 37; j++)
    {
      try
      {
        Thread.sleep(80L);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      if ((j == 36) && (Control.open)) {
        this.eastDisplay.arrive(-1);
      } else {
        this.eastDisplay.arrive(j);
      }
    }
  }
  
  public void simulateDeparture()
  {
    int delay = (int)(Math.random() * 3000.0D) + 1000;
    try
    {
      Thread.sleep(delay);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    for (int j = 0; j < 37; j++)
    {
      try
      {
        Thread.sleep(80L);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      if (j == 36) {
        this.westDisplay.depart(-1);
      } else {
        this.westDisplay.depart(j);
      }
    }
  }
  
  public static void main(String[] args)
  {
    MuseumProj museum = new MuseumProj();
    museum.init();
    
    museum.museumControl = new Thread(new Control(museum));
    museum.westExit = new Thread(new WestExit(museum));
    museum.eastEntrance = new Thread(new EastEntrance(museum));
    
    museum.museumControl.start();
    museum.westExit.start();
    museum.eastEntrance.start();
  }
}

class EastEntrance
  implements Runnable
{
  protected static volatile boolean arrival;
  private MuseumProj museum;
  private DisplayCanvas display;
  
  public EastEntrance(MuseumProj museum)
  {
    this.museum = museum;
    this.display = museum.getEastDisplay();
  }
  
  public void run()
  {
    int i = 20;
    this.display.setValue(i);
    do
    {
      this.museum.simulateArrival();
      arrival = true;
      while (!Control.allowEnter) {
        try
        {
          Thread.sleep(100L);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }
      Control.allowEnter = false;
      this.display.setValue(--i);
      try
      {
        Thread.sleep(100L);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }while ((i > 0) && (this.museum.eastEntrance != null));
  }
}

class WestExit
  implements Runnable
{
  protected static volatile boolean departure;
  private MuseumProj museum;
  private DisplayCanvas display;
  
  public WestExit(MuseumProj museum)
  {
    this.museum = museum;
    this.display = museum.getWestDisplay();
  }
  
  public void run()
  {
    int i = 0;
    this.display.setValue(i);
    while (this.museum.westExit != null)
    {
      if (Control.count > 0)
      {
        this.museum.simulateDeparture();
        departure = true;
        while (!Control.allowLeave) {
          try
          {
            Thread.sleep(100L);
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
        }
        Control.allowLeave = false;
        this.display.setValue(++i);
      }
      if (i == 20)
      {
        this.museum.museumControl = null;
        this.museum.westExit = null;
        this.museum.eastEntrance = null;
      }
      try
      {
        Thread.sleep(100L);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }
  }
}
class Control
implements Runnable
{
protected static final int MAX = 20;
protected static volatile boolean open;
protected static volatile boolean allowEnter;
protected static volatile boolean allowLeave;
protected static volatile int count;
private MuseumProj museum;
private DisplayCanvas display;

public Control(MuseumProj museum)
{
  this.museum = museum;
  this.display = museum.getMuseumDisplay();
}

public void run()
{
  open = false;
  allowEnter = false;
  allowLeave = false;
  count = 0;
  this.display.setValue(count);
  this.display.closeWestDoor();
  this.display.closeEastDoor();
  while (this.museum.museumControl != null)
  {
    if (open)
    {
      if (EastEntrance.arrival) {
        enterMuseum();
      }
      if ((count > 0) && (WestExit.departure)) {
        leaveMuseum();
      }
    }
    else if ((count > 0) && (WestExit.departure))
    {
      leaveMuseum();
    }
    try
    {
      Thread.sleep(100L);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }
}

private void enterMuseum()
{
  EastEntrance.arrival = false;
  allowEnter = true;
  this.display.setValue(++count);
}

private void leaveMuseum()
{
  WestExit.departure = false;
  allowLeave = true;
  this.display.setValue(--count);
}
}

