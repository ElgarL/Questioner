package ca.xshade.bukkit.questioner;

import ca.xshade.questionmanager.AbstractQuestion;
import ca.xshade.questionmanager.InvalidOptionException;
import ca.xshade.questionmanager.LinkedQuestion;
import ca.xshade.questionmanager.Option;
import ca.xshade.questionmanager.Poll;
import ca.xshade.questionmanager.PollQuestion;
import ca.xshade.questionmanager.PollTask;
import ca.xshade.questionmanager.Question;
import ca.xshade.questionmanager.QuestionFormatter;
import ca.xshade.questionmanager.QuestionManager;
import ca.xshade.questionmanager.QuestionTask;
import ca.xshade.util.StringMgmt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Questioner extends JavaPlugin
{
  protected QuestionManager questionManager;
  protected QuestionerPlayerListener playerListener;
  
  private List<Option> currentOptions = new ArrayList<Option>();
  private List<String> currentTargets = new ArrayList<String>();
  private int questionsPerPage = 5;
  private String questionFormat = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "%s" + ChatColor.DARK_GRAY + "] " + ChatColor.DARK_GREEN + "%s";
  private String optionFormat = ChatColor.GREEN + "          /%s";
  private String optionEntendedFormat = ChatColor.YELLOW + " : %s";
  private String listFooterFormat = ChatColor.DARK_GRAY + " ---- " + ChatColor.GRAY + "Page: %d/%d " + ChatColor.DARK_GRAY + "~" + ChatColor.GRAY + " Total Questions: %d";

  public static void main(String[] args) {
    Questioner questioner = new Questioner();
    questioner.onEnable();

    { // Insert first question
	    List<Option> options = new ArrayList<Option>();
	    options.add(new Option("yes", new QuestionTask() {
	      public void run() {
	        System.out.println(((Question)getQuestion()).getTarget() + " recieved fries!");
	      }
	    }));
	    options.add(new Option("no", new QuestionTask() {
	      public void run() {
	        System.out.println(((Question)getQuestion()).getTarget() + " slapped the worker!");
	      }
	    }));
	    Question question = new Question("You", "Would you like fries with that?", options);
	    try {
	      questioner.getQuestionManager().appendQuestion(question);
	    } catch (Exception e) {
	      System.out.println(e.getMessage());
	    }
    }

    
    { // Insert a poll
	    List<Option> options = new ArrayList<Option>();
	    options.add(new Option("rock", new PollTask()));
	    options.add(new Option("metal", new PollTask()));
	    Question question = new Question("", "What's better?", options);
	    List<String> voters = Arrays.asList(new String[] { "You" });
	    for (String voter : voters) {
	      Poll poll = new Poll(
	        voters, 
	        question) {
	        public void end() {
	          System.out.println("End of poll. Displaying results:");
	
	          System.out.println("Voters choice:");
	          HashMap<String, Option> results = getVoters();
	          for (String voter : results.keySet()) {
	            System.out.println("    " + voter + ": " + ((Option)results.get(voter)).getOptionString());
	          }
	          System.out.println("Votes:");
	          Map<Option, Integer> votes = getVotes();
	          for (Option option : votes.keySet())
	            System.out.println("    " + option.getOptionString() + ": " + votes.get(option));
	        }
	      };
	      try {
	        questioner.getQuestionManager().appendQuestion(new PollQuestion(poll, voter, question, poll.isPersistant()));
	      } catch (Exception e) {
	        System.out.println(e.getMessage());
	      }
	
	    }
    }

    { // Insert a linked question
	    List<Option> options = new ArrayList<Option>();
	    options.add(new Option("yes", new QuestionTask() {
	      public void run() {
	        System.out.println("Chris gave you a high five!");
	      }
	    }));
	    options.add(new Option("no", new QuestionTask() {
	      public void run() {
	        System.out.println("Chris slapped you!");
	      }
	    }));
	    List<String> targets = new ArrayList<String>();
	    targets.add("You");
	    targets.add("Him");
	    LinkedQuestion question = new LinkedQuestion(QuestionManager.getNextQuestionId(), targets, "Am I awesome?", options);
	    try {
	      questioner.getQuestionManager().appendLinkedQuestion(question);
	    } catch (Exception e) {
	      System.out.println(e.getMessage());
	    }
    }
    
    // Check his question list (should have one question)
	System.out.println("Peeking at his top question.");
	try {
	  AbstractQuestion question = questioner.getQuestionManager().peekAtFirstQuestion("Him");
	  
	    for (String line : QuestionFormatter.format(question))
	    System.out.println(line);
	} catch (Exception e) {
	  System.out.println(e.getMessage());
	}

	// Ask questions
    System.out.println("Answer your questions.");
    boolean hasQuestion = true;
    do {
      if (!questioner.getQuestionManager().hasQuestion("You"))
        hasQuestion = false;
      else
        try {
          AbstractQuestion question = questioner.getQuestionManager().peekAtFirstQuestion("You");

          for (String line : QuestionFormatter.format(question))
            System.out.println(line);
          do {
            Scanner input = new Scanner(System.in);
            try {
              question.getOption(input.nextLine()).getReaction().run();
              questioner.getQuestionManager().removeFirstQuestion("You");
              question = null;
            } catch (InvalidOptionException e) {
              System.out.println(e.getMessage());
            }
          }
          while (question != null);
        } catch (Exception e) {
          System.out.println(e.getMessage());
          hasQuestion = false;
        }
    } while (hasQuestion);

    // Check his question list (should be empty)
    System.out.println("Peeking at his top question.");
    try {
      AbstractQuestion question = questioner.getQuestionManager().peekAtFirstQuestion("Him");

      for (String line : QuestionFormatter.format(question))
        System.out.println(line);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  public void onDisable()
  {
    this.playerListener = null;
    this.questionManager = null;
    System.out.println("[Questioner] v" + getDescription().getVersion() + " - Disabled");
  }

  public void onEnable()
  {
    this.questionManager = new QuestionManager();
    this.playerListener = new QuestionerPlayerListener(this, this.questionManager);

    if (getServer() != null) {
      getServer().getPluginManager().registerEvents(this.playerListener, this);
      System.out.println("[Questioner] v" + getDescription().getVersion() + " - Enabled");
    }
  }

  public QuestionManager getQuestionManager() {
    return this.questionManager;
  }

  public void appendQuestion(Question question) throws Exception {
    for (Option option : question.getOptions())
      if ((option.getReaction() instanceof BukkitQuestionTask))
        ((BukkitQuestionTask)option.getReaction()).setServer(getServer());
    getQuestionManager().appendQuestion(question);

    Player player = getServer().getPlayer(question.getTarget());
    if (player != null)
      for (String line : formatQuestion(question, "New Question"))
        player.sendMessage(line);
  }

  public void sendErrorMsg(String msg) {
    System.out.println("[Questioner] Error: " + msg);
  }

  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  {
    String command = cmd.getName().toLowerCase();
    if (command.equals("question")) {
      if (args.length > 0)
      {
        Question question;
        if (sender.isOp()) {
          if (args[0].equalsIgnoreCase("target")) {
            for (int i = 1; i < args.length; i++)
              this.currentTargets.add(args[i]);
            sender.sendMessage("NumTargets: " + this.currentTargets.size());
            return true;
          }if (args[0].equalsIgnoreCase("opt")) {
            if (args.length > 1) {
              this.currentOptions.add(new Option(args[1], new QuestionTask() {
                public void run() {
                  System.out.println("You chose " + getOption().getOptionString() + "!");
                }
              }));
              sender.sendMessage("NumOptions: " + this.currentOptions.size());
            } else {
              sender.sendMessage("help > question opt [option]");
            }
            return true;
          }if (args[0].equalsIgnoreCase("ask")) {
            try {
              String q = StringMgmt.join(StringMgmt.remFirstArg(args), " ");
              for (String target : this.currentTargets) {
                question = new Question(target.toLowerCase(), q, this.currentOptions);
                appendQuestion(question);
              }
              this.currentOptions.clear();
              this.currentTargets.clear();
            } catch (Exception e) {
              System.out.println(e.getMessage());
            }
            return true;
          }
        }
        if ((args[0].equalsIgnoreCase("list")) && 
          ((sender instanceof Player))) {
          Player player = (Player)sender;
          int page = 1;
          if (args.length > 1)
            try {
              page = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException localNumberFormatException1) {
            }
          for (String line : formatQuestionList(player.getName(), page))
            player.sendMessage(line);
          return true;
        }

      }

      sender.sendMessage("Invalid sub command.");
      return true;
    }

    return false;
  }

  public List<String> formatQuestionList(String user, int page) {
    List<String> out = new ArrayList<String>();
    try {
      if (page < 0)
        throw new Exception("Invalid page number.");

      LinkedList<AbstractQuestion> activePlayerQuestions = getQuestionManager().getQuestions(user);
      int numQuestions = activePlayerQuestions.size();
      int maxPage = (int)Math.ceil(numQuestions / (double)this.questionsPerPage);
      if (page > maxPage) {
        throw new Exception("There are no questions on page " + page);
      } else {
	      int start = (page - 1) * this.questionsPerPage;
	      for (int i = start; i < start + this.questionsPerPage; i++)
	        try {
	          AbstractQuestion question = (AbstractQuestion)activePlayerQuestions.get(i);
	          out.addAll(formatQuestion(question, Integer.toString(i)));
	        } catch (IndexOutOfBoundsException e) {
	        }
      }
      if (maxPage > 1)
        out.add(String.format(this.listFooterFormat, new Object[] { Integer.valueOf(page), Integer.valueOf(maxPage), Integer.valueOf(numQuestions) }));
    } catch (Exception e) {
      out.add(ChatColor.RED + e.getMessage());
    }
    return out;
  }

  public List<String> formatQuestion(AbstractQuestion question, String tag) {
    List<String> out = new ArrayList<String>();
    out.add(String.format(this.questionFormat, new Object[] { tag, StringMgmt.maxLength(question.getQuestion(), 54) }));
    for (Option option : question.getOptions())
      out.add(String.format(this.optionFormat, new Object[] { option.toString() }) + (option.hasDescription() ? String.format(this.optionEntendedFormat, new Object[] { option.getOptionDescription() }) : ""));
    return out;
  }

  public void loadClasses() {
    String[] classes = { 
      "ca.xshade.bukkit.questioner.BukkitQuestionTask", 
      "ca.xshade.questionmanager.AbstractQuestion", 
      "ca.xshade.questionmanager.InvalidOptionException", 
      "ca.xshade.questionmanager.LinkedQuestion", 
      "ca.xshade.questionmanager.LinkedQuestionTask", 
      "ca.xshade.questionmanager.Option", 
      "ca.xshade.questionmanager.OptionTask", 
      "ca.xshade.questionmanager.Poll", 
      "ca.xshade.questionmanager.PollQuestion", 
      "ca.xshade.questionmanager.PollTask", 
      "ca.xshade.questionmanager.Question", 
      "ca.xshade.questionmanager.QuestionFormatter", 
      "ca.xshade.questionmanager.QuestionManager", 
      "ca.xshade.questionmanager.QuestionTask" };

    for (String c : classes)
      try {
        Questioner.class.getClassLoader().loadClass(c);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
  }
}