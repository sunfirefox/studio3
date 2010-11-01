package com.aptana.git.ui.internal.actions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.aptana.git.core.model.GitRepository;
import com.aptana.ui.MenuDialog;
import com.aptana.ui.MenuDialogItem;

public class DeleteBranchHandler extends AbstractGitHandler
{

	private static final int TOOLTIP_LIFETIME = 3000;

	@Override
	protected Object doExecute(ExecutionEvent event) throws ExecutionException
	{
		final GitRepository repo = getSelectedRepository();
		if (repo == null)
		{
			return null;
		}

		String currentBranch = repo.currentBranch();
		List<MenuDialogItem> listOfMaps = new ArrayList<MenuDialogItem>();
		for (String branch : repo.localBranches())
		{
			if (branch.equals(currentBranch))
			{
				continue;
			}
			listOfMaps.add(new MenuDialogItem(branch));
		}
		MenuDialog dialog = new MenuDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
		dialog.setInput(listOfMaps);
		if (dialog.open() == Window.OK)
		{
			MenuDialogItem item = listOfMaps.get(dialog.getReturnCode());
			deleteBranch(repo, item.getText());
		}
		return null;
	}

	public static void deleteBranch(final GitRepository repo, final String branchName)
	{
		Job job = new Job(NLS.bind(Messages.DeleteBranchHandler_JobName, branchName))
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				IStatus status = repo.deleteBranch(branchName);
				if (!status.isOK())
				{
					final IStatus theStatus = status;
					final boolean[] result = new boolean[1];
					// Failed, show reason why to user and ask if they want to force with -D
					Display.getDefault().syncExec(new Runnable()
					{

						public void run()
						{
							result[0] = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
									Messages.DeleteBranchAction_BranchDeletionFailed_Title, MessageFormat.format(
											Messages.DeleteBranchAction_BranchDeletionFailed_Msg, branchName,
											theStatus.getMessage()));
						}
					});
					if (result[0])
					{
						status = repo.deleteBranch(branchName, true); // re-run with force switch as user has consented
					}
					else
					{
						// Just return a bogus OK status, since we already bugged user about it
						return Status.OK_STATUS;
					}
				}
				if (status.isOK())
				{
					// Now show a tooltip "toast" for 3 seconds to announce success
					showSuccessToast(branchName);
				}
				return status;
			}
		};
		job.setUser(true);
		job.setPriority(Job.SHORT);
		job.schedule();
	}

	private static void showSuccessToast(final String branchName)
	{
		Job job = new UIJob("show toast") //$NON-NLS-1$
		{

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				Display display = PlatformUI.getWorkbench().getDisplay();
				if (display == null)
				{
					display = Display.getDefault();
				}
				Shell aShell = display.getActiveShell();
				if (aShell == null)
				{
					aShell = new Shell(display);
				}
				final Shell shell = aShell;
				String text = MessageFormat.format(Messages.DeleteBranchAction_BranchDelete_Msg, branchName);
				DefaultToolTip toolTip = new DefaultToolTip(shell)
				{
					@Override
					public Point getLocation(Point size, Event event)
					{
						final Rectangle workbenchWindowBounds = shell.getBounds();
						int xCoord = workbenchWindowBounds.x + workbenchWindowBounds.width - size.x - 10;
						int yCoord = workbenchWindowBounds.y + workbenchWindowBounds.height - size.y - 10;
						return new Point(xCoord, yCoord);
					}
				};
				toolTip.setHideDelay(TOOLTIP_LIFETIME);
				toolTip.setText(text);
				toolTip.show(new Point(0, 0));
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}
}
