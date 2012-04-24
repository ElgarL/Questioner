package ca.xshade.bukkit.questioner;

import ca.xshade.questionmanager.QuestionTask;

import org.bukkit.Server;

public abstract class BukkitQuestionTask extends QuestionTask
{
  protected Server server;

  public Server getServer()
  {
    return this.server;
  }

  public void setServer(Server server) {
    this.server = server;
  }

  public abstract void run();
}