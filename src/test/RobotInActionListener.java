package test;


import java.awt.*;
import java.awt.event.*;

import burp.RobotInput;

class RobotInActionListener extends Frame implements ActionListener
{
	RobotInActionListener (String title)
	{
		super (title);

		addWindowListener (new WindowAdapter ()
		{
			public void windowClosing (WindowEvent e)
			{
				System.exit (0);
			}
		});

		Panel p = new Panel ();
		Button b = new Button ("Press Me");
		b.addActionListener (this);
		p.add (b);

		add (p);

		setSize (175, 100);
		setVisible (true);
	}

	public void actionPerformed111 (ActionEvent e)
	{
		try
		{
			Runtime.getRuntime ().exec ("notepad.exe");
		}
		catch (java.io.IOException e2) { System.out.println (e2);}

		try
		{
			Thread.sleep (1000);
		}
		catch (InterruptedException e2) {}

		try
		{
			Robot r = new Robot ();

			int [] keys =
				{
						KeyEvent.VK_T,
						KeyEvent.VK_E,
						KeyEvent.VK_X,
						KeyEvent.VK_T,
						KeyEvent.VK_ENTER
				};

			for (int i = 0; i < keys.length; i++)
			{
				r.keyPress (keys [i]);
				r.keyRelease (keys [i]);
			}

			Toolkit tk = Toolkit.getDefaultToolkit ();
			Dimension dim = tk.getScreenSize ();

			r.mouseMove (dim.width / 2, dim.height / 2);
		}
		catch (AWTException e2) {}
	}
	
	public void actionPerformed (ActionEvent e){
		try {
			String selectedUrl = new RobotInput().getSelectedString();
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	
	public static void main (String [] args)
	{
		new RobotInActionListener ("Robot Demo");
	}
}