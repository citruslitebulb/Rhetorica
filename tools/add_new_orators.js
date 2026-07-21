/**
 * One-shot generator: append six new orators + seed packs.
 * Safe to re-run: replaces entries/files for ids 39–44 only.
 */
const fs = require("fs");
const path = require("path");

const seed = path.join(__dirname, "..", "app", "src", "main", "assets", "data", "seed");
const loaderPath = path.join(
  __dirname,
  "..",
  "app",
  "src",
  "main",
  "java",
  "com",
  "rhetorica",
  "app",
  "data",
  "seed",
  "SeedDataLoader.kt",
);
const portraitsPath = path.join(
  __dirname,
  "..",
  "app",
  "src",
  "main",
  "java",
  "com",
  "rhetorica",
  "app",
  "core",
  "model",
  "OratorPortraits.kt",
);

const NEW_ORATORS = [
  {
    id: 39,
    slug: "theodore_roosevelt",
    file: "roosevelt",
    dict: {
      id: 39,
      name: "Theodore Roosevelt",
      description:
        "26th U.S. President whose bully pulpit and 'Man in the Arena' defined strenuous civic life",
      oratorName: "Theodore Roosevelt",
      wordCount: 16,
      category: "20th Century Legends",
      era: "Progressive Era (1858-1919)",
      bio: "Theodore Roosevelt was the 26th President of the United States, a Rough Rider, conservationist, and reformer. His speeches celebrated the strenuous life, honest effort, and the citizen who dares greatly even when imperfect.",
      portraitUrl: "",
      primaryStyle: "Vigorous moral exhortation",
      voiceStyle: "Energetic, plainspoken, bracing",
      colorAccent: 10824234,
      sampleSpeech:
        "It is not the critic who counts; not the man who points out how the strong man stumbles. The credit belongs to the man who is actually in the arena.",
      tags: ["Progressive", "Courage", "Citizenship", "Action"],
      themeCategories: ["courage", "leadership", "democracy", "inspirational", "legacy"],
      isActive: true,
    },
    words: [
      {
        word: "arena",
        definition: "The public field of contest and risk where real effort is tested.",
        example:
          "The credit belongs to the man who is actually in the arena, whose face is marred by dust and sweat and blood.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Citizenship in a Republic",
        speech: "Citizenship in a Republic (Sorbonne)",
        categories: ["courage", "inspirational", "leadership"],
      },
      {
        word: "critic",
        definition: "One who judges from the sidelines without bearing the cost of action.",
        example:
          "It is not the critic who counts; not the man who points out how the strong man stumbles.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Citizenship in a Republic",
        speech: "Citizenship in a Republic (Sorbonne)",
        categories: ["courage", "humanities"],
      },
      {
        word: "strive",
        definition: "To struggle earnestly toward a worthy goal despite failure.",
        example:
          "Who strives valiantly; who errs, who comes short again and again, because there is no effort without error and shortcoming.",
        partOfSpeech: "verb",
        complexity: "intermediate",
        source: "Citizenship in a Republic",
        speech: "Citizenship in a Republic (Sorbonne)",
        categories: ["courage", "inspirational"],
      },
      {
        word: "devotion",
        definition: "Loyal dedication of energy to a cause larger than comfort.",
        example:
          "Who spends himself in a worthy cause; who at the best knows in the end the triumph of high achievement, and who at the worst, if he fails, at least fails while daring greatly—that devotion is what the arena measures.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Citizenship in a Republic",
        speech: "Citizenship in a Republic (Sorbonne)",
        categories: ["courage", "leadership", "legacy"],
      },
      {
        word: "strenuous",
        definition: "Requiring great effort; marked by vigorous, demanding action.",
        example:
          "I wish to preach, not the doctrine of ignoble ease, but the doctrine of the strenuous life.",
        partOfSpeech: "adjective",
        complexity: "advanced",
        source: "The Strenuous Life",
        speech: "The Strenuous Life",
        categories: ["courage", "inspirational", "leadership"],
      },
      {
        word: "ignoble",
        definition: "Not honorable in character or purpose; base or petty.",
        example:
          "I wish to preach, not the doctrine of ignoble ease, but the doctrine of the strenuous life.",
        partOfSpeech: "adjective",
        complexity: "advanced",
        source: "The Strenuous Life",
        speech: "The Strenuous Life",
        categories: ["humanities", "courage"],
      },
      {
        word: "bully",
        definition:
          "As Roosevelt used it: excellent, first-rate—especially of a public platform for persuasion.",
        example:
          "I suppose my critics will call that preaching; I call it using the bully pulpit.",
        partOfSpeech: "adjective",
        complexity: "intermediate",
        source: "White House remarks (popular attribution)",
        speech: "The bully pulpit",
        categories: ["leadership", "democracy"],
      },
      {
        word: "pulpit",
        definition: "A raised platform for speaking; metaphorically, a position of moral influence.",
        example:
          "I suppose my critics will call that preaching; I call it using the bully pulpit.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "White House remarks (popular attribution)",
        speech: "The bully pulpit",
        categories: ["leadership", "democracy", "humanities"],
      },
      {
        word: "square",
        definition: "Fair and honest dealing; a just bargain between parties.",
        example: "We demand that big business give the people a square deal.",
        partOfSpeech: "adjective",
        complexity: "basic",
        source: "Square Deal speeches",
        speech: "The Square Deal",
        categories: ["democracy", "leadership"],
      },
      {
        word: "conservation",
        definition: "Protection and wise use of natural resources for the long term.",
        example:
          "Conservation means development as much as it does protection; the earth and its resources are for the people.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Conservation addresses",
        speech: "Conservation as a National Duty",
        categories: ["leadership", "legacy", "humanities"],
      },
      {
        word: "citizenship",
        definition: "The duties and character of a free person in a republic.",
        example:
          "Citizenship in a republic means more than voting once; it means standing in the arena when the work is hard.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Citizenship in a Republic",
        speech: "Citizenship in a Republic (Sorbonne)",
        categories: ["democracy", "leadership", "legacy"],
      },
      {
        word: "daring",
        definition: "Willing to take bold risks; adventurous courage in action.",
        example:
          "If he fails, at least he fails while daring greatly, so that his place shall never be with those cold and timid souls who neither know victory nor defeat.",
        partOfSpeech: "adjective",
        complexity: "basic",
        source: "Citizenship in a Republic",
        speech: "Citizenship in a Republic (Sorbonne)",
        categories: ["courage", "inspirational"],
      },
      {
        word: "timid",
        definition: "Showing a lack of courage or confidence; easily frightened.",
        example:
          "His place shall never be with those cold and timid souls who neither know victory nor defeat.",
        partOfSpeech: "adjective",
        complexity: "intermediate",
        source: "Citizenship in a Republic",
        speech: "Citizenship in a Republic (Sorbonne)",
        categories: ["courage", "humanities"],
      },
      {
        word: "triumph",
        definition: "A great victory or achievement after struggle.",
        example:
          "Who at the best knows in the end the triumph of high achievement, and who at the worst, if he fails, at least fails while daring greatly.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Citizenship in a Republic",
        speech: "Citizenship in a Republic (Sorbonne)",
        categories: ["inspirational", "courage", "legacy"],
      },
      {
        word: "reform",
        definition: "To improve by removing abuses or correcting faults in institutions.",
        example:
          "Reform is not a pastime for the idle; it is the hard work of making institutions serve free people.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Progressive campaign addresses",
        speech: "Progressive reform addresses",
        categories: ["democracy", "leadership"],
      },
      {
        word: "character",
        definition: "Moral quality revealed by conduct under pressure.",
        example:
          "Character, in the long run, is the decisive factor in the life of an individual and of nations alike.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Various addresses",
        speech: "Citizenship and character",
        categories: ["leadership", "humanities", "legacy"],
      },
    ],
    quotes: [
      {
        text: "It is not the critic who counts; not the man who points out how the strong man stumbles, or where the doer of deeds could have done them better. The credit belongs to the man who is actually in the arena.",
        source: "Citizenship in a Republic",
        speech: "Citizenship in a Republic (Sorbonne, Paris)",
        year: 1910,
        context: "Defining the moral worth of effort over mere commentary.",
      },
      {
        text: "I wish to preach, not the doctrine of ignoble ease, but the doctrine of the strenuous life.",
        source: "The Strenuous Life",
        speech: "The Strenuous Life (Chicago)",
        year: 1899,
        context: "Urging Americans toward vigorous public and private effort.",
      },
      {
        text: "Speak softly and carry a big stick; you will go far.",
        source: "West African proverb, popularized by Roosevelt",
        speech: "Foreign policy remarks",
        year: 1901,
        context: "Summarizing a diplomacy of restraint backed by strength.",
      },
      {
        text: "The government is us; we are the government, you and I.",
        source: "Campaign and civic addresses",
        speech: "Civic responsibility remarks",
        year: 1902,
        context: "Linking democratic power to ordinary citizens.",
      },
      {
        text: "Do what you can, with what you have, where you are.",
        source: "Attributed counsel",
        speech: "Practical citizenship",
        year: 1903,
        context: "A compact rule for useful action without excuses.",
      },
      {
        text: "A vote is like a rifle: its usefulness depends upon the character of the user.",
        source: "Civic addresses",
        speech: "Citizenship and the ballot",
        year: 1904,
        context: "Tying suffrage to moral responsibility.",
      },
      {
        text: "The best executive is the one who has sense enough to pick good men to do what he wants done, and self-restraint enough to keep from meddling with them while they do it.",
        source: "Leadership maxims",
        speech: "On executive leadership",
        year: 1905,
        context: "Delegating authority without abandoning responsibility.",
      },
      {
        text: "Conservation is a great moral issue, for it involves the patriotic duty of ensuring the safety and continuance of the nation.",
        source: "Conservation addresses",
        speech: "Conservation as a National Duty",
        year: 1908,
        context: "Framing resource protection as civic duty.",
      },
    ],
    speech: {
      title: "Citizenship in a Republic (The Man in the Arena)",
      year: 1910,
      description:
        "Excerpt from Theodore Roosevelt's address at the Sorbonne, Paris—his famous defense of those who dare greatly.",
      fullText: `It is not the critic who counts; not the man who points out how the strong man stumbles, or where the doer of deeds could have done them better.

The credit belongs to the man who is actually in the arena, whose face is marred by dust and sweat and blood; who strives valiantly; who errs, who comes short again and again, because there is no effort without error and shortcoming; but who does actually strive to do the deeds; who knows great enthusiasms, the great devotions; who spends himself in a worthy cause; who at the best knows in the end the triumph of high achievement, and who at the worst, if he fails, at least fails while daring greatly, so that his place shall never be with those cold and timid souls who neither know victory nor defeat.

Far better it is to dare mighty things, to win glorious triumphs, even though checkered by failure, than to take rank with those poor spirits who neither enjoy much nor suffer much, because they live in the gray twilight that knows not victory nor defeat.`,
    },
  },
  {
    id: 40,
    slug: "ronald_reagan",
    file: "reagan",
    dict: {
      id: 40,
      name: "Ronald Reagan",
      description:
        "40th U.S. President, the Great Communicator who blended optimism with a hard line against tyranny",
      oratorName: "Ronald Reagan",
      wordCount: 16,
      category: "20th Century Legends",
      era: "Cold War era (1911-2004)",
      bio: "Ronald Reagan was the 40th President of the United States and a former actor and governor. His speeches mixed sunny patriotism with moral clarity about freedom and the Cold War, earning him the nickname 'the Great Communicator.'",
      portraitUrl: "",
      primaryStyle: "Optimistic moral clarity",
      voiceStyle: "Warm, conversational, firm",
      colorAccent: 2184192,
      sampleSpeech:
        "Mr. Gorbachev, tear down this wall! If you seek peace, if you seek prosperity for the Soviet Union and Eastern Europe, come here to this gate.",
      tags: ["Cold War", "Freedom", "Optimism", "Diplomacy"],
      themeCategories: ["democracy", "leadership", "courage", "inspirational", "legacy"],
      isActive: true,
    },
    words: [
      {
        word: "freedom",
        definition: "The power to act, speak, and think without unjust restraint.",
        example:
          "Freedom is never more than one generation away from extinction. We didn't pass it to our children in the bloodstream.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Various addresses",
        speech: "On freedom's inheritance",
        categories: ["democracy", "inspirational", "legacy"],
      },
      {
        word: "extinction",
        definition: "The state of being destroyed or ending permanently.",
        example:
          "Freedom is never more than one generation away from extinction. We didn't pass it to our children in the bloodstream.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Various addresses",
        speech: "On freedom's inheritance",
        categories: ["democracy", "courage"],
      },
      {
        word: "prosperity",
        definition: "A condition of economic well-being and thriving.",
        example:
          "If you seek peace, if you seek prosperity for the Soviet Union and Eastern Europe, if you seek liberalization: come here to this gate.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Berlin Wall Address",
        speech: "Tear Down This Wall (Brandenburg Gate)",
        categories: ["leadership", "democracy"],
      },
      {
        word: "liberalization",
        definition: "The process of making laws or systems less restrictive.",
        example:
          "If you seek peace, if you seek prosperity for the Soviet Union and Eastern Europe, if you seek liberalization: come here to this gate.",
        partOfSpeech: "noun",
        complexity: "advanced",
        source: "Berlin Wall Address",
        speech: "Tear Down This Wall (Brandenburg Gate)",
        categories: ["democracy", "leadership"],
      },
      {
        word: "gate",
        definition: "An opening that controls entry; here, a political threshold of change.",
        example:
          "Mr. Gorbachev, open this gate! Mr. Gorbachev, tear down this wall!",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Berlin Wall Address",
        speech: "Tear Down This Wall (Brandenburg Gate)",
        categories: ["democracy", "courage", "legacy"],
      },
      {
        word: "evil",
        definition: "Profoundly immoral and destructive force or system.",
        example:
          "I urge you to beware the temptation of pride—the temptation of blithely declaring yourselves above it all and label both sides equally at fault, to ignore the facts of history and the aggressive impulses of an evil empire.",
        partOfSpeech: "adjective",
        complexity: "basic",
        source: "Evil Empire Speech",
        speech: "Evil Empire (National Association of Evangelicals)",
        categories: ["courage", "humanities", "leadership"],
      },
      {
        word: "empire",
        definition: "A vast political dominion; here, a system of coercive power.",
        example:
          "I urge you to beware the temptation of pride—the temptation to ignore the facts of history and the aggressive impulses of an evil empire.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Evil Empire Speech",
        speech: "Evil Empire (National Association of Evangelicals)",
        categories: ["courage", "leadership", "legacy"],
      },
      {
        word: "morning",
        definition: "The beginning of day; metaphorically, a renewed national spirit.",
        example: "It's morning again in America—and under the leadership of hope, work seems possible again.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "1984 campaign theme",
        speech: "Morning in America",
        categories: ["inspirational", "leadership"],
      },
      {
        word: "shining",
        definition: "Giving off or reflecting bright light; radiantly exemplary.",
        example:
          "I've spoken of the shining city all my political life, but I don't know if I ever quite communicated what I saw when I said it.",
        partOfSpeech: "adjective",
        complexity: "intermediate",
        source: "Farewell Address",
        speech: "Farewell Address to the Nation",
        categories: ["inspirational", "democracy", "legacy"],
      },
      {
        word: "city",
        definition: "A large community; in Reagan's image, the American ideal made visible.",
        example:
          "In my mind it was a tall, proud city built on rocks stronger than oceans, wind-swept, God-blessed, and teeming with people of all kinds living in harmony and peace.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Farewell Address",
        speech: "Farewell Address to the Nation",
        categories: ["democracy", "inspirational", "legacy"],
      },
      {
        word: "trust",
        definition: "Firm belief in reliability, truth, or ability.",
        example:
          "The nine most terrifying words in the English language are: I'm from the government, and I'm here to help—a line that mocked misplaced trust in bureaucracy.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Press conference quip",
        speech: "On government and trust",
        categories: ["democracy", "leadership"],
      },
      {
        word: "negotiate",
        definition: "To discuss in order to reach an agreement.",
        example:
          "We can negotiate from strength; peace is not the absence of principle, but the product of clear purpose.",
        partOfSpeech: "verb",
        complexity: "intermediate",
        source: "Arms control addresses",
        speech: "Peace through strength",
        categories: ["leadership", "courage"],
      },
      {
        word: "strength",
        definition: "Capacity for force or endurance; power to resist pressure.",
        example:
          "We maintain strength not to make war, but so that peace can be negotiated without surrender.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Defense and diplomacy addresses",
        speech: "Peace through strength",
        categories: ["leadership", "courage", "legacy"],
      },
      {
        word: "dream",
        definition: "A cherished aspiration or ideal for the future.",
        example:
          "America is too great for small dreams; our dream is large enough to include the stranger at the gate.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Inaugural and campaign addresses",
        speech: "On the American dream",
        categories: ["inspirational", "democracy"],
      },
      {
        word: "hero",
        definition: "A person admired for courage, outstanding achievements, or noble qualities.",
        example:
          "Those who say that we are in a time when there are no heroes just don't know where to look—heroes still walk among us.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "First Inaugural Address",
        speech: "First Inaugural Address",
        categories: ["inspirational", "courage", "legacy"],
      },
      {
        word: "peace",
        definition: "Freedom from war and violent conflict; ordered calm among nations.",
        example:
          "Peace is not absence of conflict; it is the ability to handle conflict by peaceful means while liberty stands.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Diplomatic addresses",
        speech: "On peace and liberty",
        categories: ["leadership", "democracy", "inspirational"],
      },
    ],
    quotes: [
      {
        text: "Mr. Gorbachev, tear down this wall!",
        source: "Berlin Wall Address",
        speech: "Remarks at the Brandenburg Gate",
        year: 1987,
        context: "Challenging the Soviet leader to open East Berlin.",
      },
      {
        text: "Freedom is never more than one generation away from extinction. We didn't pass it to our children in the bloodstream. It must be fought for, protected, and handed on.",
        source: "Various addresses",
        speech: "On freedom's inheritance",
        year: 1961,
        context: "Warning that liberty requires active stewardship.",
      },
      {
        text: "I've spoken of the shining city all my political life.",
        source: "Farewell Address",
        speech: "Farewell Address to the Nation",
        year: 1989,
        context: "Recalling his metaphor for America's promise.",
      },
      {
        text: "Government is not the solution to our problem; government is the problem.",
        source: "First Inaugural Address",
        speech: "First Inaugural Address",
        year: 1981,
        context: "Framing limited government as a governing philosophy.",
      },
      {
        text: "There is no limit to the amount of good you can do if you don't care who gets the credit.",
        source: "Attributed maxim used in speeches",
        speech: "On service without vanity",
        year: 1981,
        context: "Praising selfless public work.",
      },
      {
        text: "We will always remember. We will always be proud. We will always be prepared, so we may always be free.",
        source: "D-Day commemorations",
        speech: "Pointe du Hoc / D-Day remembrances",
        year: 1984,
        context: "Honoring Allied sacrifice and readiness.",
      },
      {
        text: "The future doesn't belong to the fainthearted; it belongs to the brave.",
        source: "Challenger disaster address",
        speech: "Address to the Nation on the Challenger",
        year: 1986,
        context: "Comforting a grieving nation after the shuttle tragedy.",
      },
      {
        text: "Peace is not absence of conflict, it is the ability to handle conflict by peaceful means.",
        source: "Diplomatic remarks",
        speech: "On peace",
        year: 1982,
        context: "Defining peace as skilled conflict resolution.",
      },
    ],
    speech: {
      title: "Tear Down This Wall",
      year: 1987,
      description:
        "Excerpt from Ronald Reagan's remarks at the Brandenburg Gate challenging the division of Berlin.",
      fullText: `We come to Berlin, we American presidents, because it's our duty to speak, in this place, of freedom.

Behind me stands a wall that encircles the free sectors of this city, part of a vast system of barriers that divides the entire continent of Europe. From the Baltic south, those barriers cut across Germany in a gash of barbed wire, concrete, dog runs, and guard towers.

General Secretary Gorbachev, if you seek peace, if you seek prosperity for the Soviet Union and Eastern Europe, if you seek liberalization: Come here to this gate.

Mr. Gorbachev, open this gate!

Mr. Gorbachev, tear down this wall!

As I looked out a moment ago from the Reichstag, that embodiment of German unity, I noticed words crudely spray-painted upon the wall, perhaps by a young Berliner: "This wall will fall. Beliefs become reality."

Yes, across Europe, this wall will fall. For it cannot withstand faith; it cannot withstand truth. The wall cannot withstand freedom.`,
    },
  },
  {
    id: 41,
    slug: "eleanor_roosevelt",
    file: "eleanor",
    dict: {
      id: 41,
      name: "Eleanor Roosevelt",
      description:
        "First Lady, diplomat, and human-rights champion who helped draft the Universal Declaration of Human Rights",
      oratorName: "Eleanor Roosevelt",
      wordCount: 16,
      category: "20th Century Legends",
      era: "20th Century (1884-1962)",
      bio: "Eleanor Roosevelt transformed the role of First Lady into a platform for justice, then served as a U.S. delegate to the United Nations. She chaired the commission that produced the Universal Declaration of Human Rights and spoke with moral authority about dignity, courage, and everyday citizenship.",
      portraitUrl: "",
      primaryStyle: "Moral clarity with practical empathy",
      voiceStyle: "Warm, steady, insistent",
      colorAccent: 7220068,
      sampleSpeech:
        "You gain strength, courage and confidence by every experience in which you really stop to look fear in the face. You must do the thing you think you cannot do.",
      tags: ["Human rights", "Courage", "Diplomacy", "Equality"],
      themeCategories: ["courage", "democracy", "humanities", "inspirational", "legacy"],
      isActive: true,
    },
    words: [
      {
        word: "dignity",
        definition: "The state of being worthy of honor and respect.",
        example:
          "Where, after all, do universal human rights begin? In small places, close to home—so close and so small that they cannot be seen on any maps of the world—yet they are the world of the individual person: the neighborhood he lives in; the school or college he attends; the factory, farm or office where he works. Such are the places where every man, woman and child seeks equal justice, equal opportunity, equal dignity without discrimination.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "On the Universal Declaration of Human Rights",
        speech: "The Great Question (UN remarks)",
        categories: ["humanities", "democracy", "legacy"],
      },
      {
        word: "universal",
        definition: "Applicable everywhere and to all people without exception.",
        example:
          "The Universal Declaration of Human Rights was meant to be a common standard of achievement for all peoples and all nations.",
        partOfSpeech: "adjective",
        complexity: "intermediate",
        source: "UN human rights work",
        speech: "On the Universal Declaration",
        categories: ["democracy", "humanities", "legacy"],
      },
      {
        word: "fear",
        definition: "An unpleasant emotion caused by the threat of danger or pain.",
        example:
          "You gain strength, courage and confidence by every experience in which you really stop to look fear in the face.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "You Learn by Living",
        speech: "On courage and fear",
        categories: ["courage", "inspirational"],
      },
      {
        word: "confidence",
        definition: "A feeling of self-assurance arising from ability or experience.",
        example:
          "You gain strength, courage and confidence by every experience in which you really stop to look fear in the face.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "You Learn by Living",
        speech: "On courage and fear",
        categories: ["courage", "inspirational", "leadership"],
      },
      {
        word: "curiosity",
        definition: "A strong desire to know or learn something.",
        example:
          "I think, at a child's birth, if a mother could ask a fairy godmother to endow it with the most useful gift, that gift should be curiosity.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Essays and lectures",
        speech: "On education and curiosity",
        categories: ["humanities", "inspirational"],
      },
      {
        word: "justice",
        definition: "Fair treatment according to what is due or merited.",
        example:
          "Such are the places where every man, woman and child seeks equal justice, equal opportunity, equal dignity without discrimination.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "On the Universal Declaration of Human Rights",
        speech: "The Great Question (UN remarks)",
        categories: ["democracy", "humanities", "courage"],
      },
      {
        word: "opportunity",
        definition: "A set of circumstances that makes it possible to do something.",
        example:
          "Such are the places where every man, woman and child seeks equal justice, equal opportunity, equal dignity without discrimination.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "On the Universal Declaration of Human Rights",
        speech: "The Great Question (UN remarks)",
        categories: ["democracy", "inspirational"],
      },
      {
        word: "discrimination",
        definition: "Unjust treatment of people based on group identity rather than merit.",
        example:
          "Equal dignity without discrimination is the quiet test of whether rights are real in daily life.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "On the Universal Declaration of Human Rights",
        speech: "The Great Question (UN remarks)",
        categories: ["democracy", "humanities", "courage"],
      },
      {
        word: "citizen",
        definition: "A member of a political community with rights and duties.",
        example:
          "The destiny of human rights is in the hands of all our citizens in all our communities.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "On the Universal Declaration of Human Rights",
        speech: "The Great Question (UN remarks)",
        categories: ["democracy", "leadership", "legacy"],
      },
      {
        word: "future",
        definition: "Time yet to come; the period in which consequences unfold.",
        example:
          "The future belongs to those who believe in the beauty of their dreams—and work to make them less fragile.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Attributed counsel",
        speech: "On dreams and the future",
        categories: ["inspirational", "legacy"],
      },
      {
        word: "beauty",
        definition: "A quality that pleases the mind as a high ideal of form or spirit.",
        example:
          "The future belongs to those who believe in the beauty of their dreams.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Attributed counsel",
        speech: "On dreams and the future",
        categories: ["inspirational", "arts", "humanities"],
      },
      {
        word: "hate",
        definition: "Intense hostility; the will to diminish another.",
        example:
          "People grow through experience if they meet life honestly and courageously. This is how character is built—and how hate is starved of fuel.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Essays on living",
        speech: "On character and hate",
        categories: ["humanities", "courage"],
      },
      {
        word: "light",
        definition: "Illumination; metaphorically, understanding that dispels ignorance.",
        example:
          "It is better to light a candle than to curse the darkness.",
        partOfSpeech: "verb",
        complexity: "basic",
        source: "Popular maxim associated with her work",
        speech: "On light and action",
        categories: ["inspirational", "courage", "legacy"],
      },
      {
        word: "candle",
        definition: "A small source of flame; a symbol of modest, useful action.",
        example:
          "It is better to light a candle than to curse the darkness.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Popular maxim associated with her work",
        speech: "On light and action",
        categories: ["inspirational", "humanities"],
      },
      {
        word: "right",
        definition: "A moral or legal entitlement to have or do something.",
        example:
          "Where, after all, do universal human rights begin? In small places, close to home.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "On the Universal Declaration of Human Rights",
        speech: "The Great Question (UN remarks)",
        categories: ["democracy", "humanities", "legacy"],
      },
      {
        word: "courage",
        definition: "The ability to do something that frightens one; strength in the face of pain.",
        example:
          "You gain strength, courage and confidence by every experience in which you really stop to look fear in the face. You must do the thing you think you cannot do.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "You Learn by Living",
        speech: "On courage and fear",
        categories: ["courage", "inspirational", "leadership"],
      },
    ],
    quotes: [
      {
        text: "You gain strength, courage and confidence by every experience in which you really stop to look fear in the face. You must do the thing you think you cannot do.",
        source: "You Learn by Living",
        speech: "On courage",
        year: 1960,
        context: "Defining courage as practiced action against fear.",
      },
      {
        text: "Where, after all, do universal human rights begin? In small places, close to home—so close and so small that they cannot be seen on any maps of the world.",
        source: "Remarks on the Universal Declaration",
        speech: "The Great Question",
        year: 1958,
        context: "Rooting human rights in daily life, not abstractions.",
      },
      {
        text: "The future belongs to those who believe in the beauty of their dreams.",
        source: "Attributed",
        speech: "On dreams",
        year: 1940,
        context: "Encouraging hopeful imagination paired with work.",
      },
      {
        text: "No one can make you feel inferior without your consent.",
        source: "This Is My Story / later popular form",
        speech: "On self-respect",
        year: 1937,
        context: "On refusing internalized contempt.",
      },
      {
        text: "It is better to light a candle than to curse the darkness.",
        source: "Associated maxim",
        speech: "On constructive action",
        year: 1945,
        context: "Preferring small useful deeds over pure complaint.",
      },
      {
        text: "Do what you feel in your heart to be right—for you'll be criticized anyway.",
        source: "Advice often quoted from her letters and talks",
        speech: "On conscience",
        year: 1944,
        context: "Acting on conscience despite inevitable critics.",
      },
      {
        text: "Great minds discuss ideas; average minds discuss events; small minds discuss people.",
        source: "Attributed aphorism popularized in her circles",
        speech: "On conversation and character",
        year: 1940,
        context: "Elevating discourse toward ideas.",
      },
      {
        text: "When will our consciences grow so tender that we will act to prevent human misery rather than avenge it?",
        source: "Newspaper columns and talks",
        speech: "On conscience and prevention",
        year: 1946,
        context: "Pushing prevention over reactive vengeance.",
      },
    ],
    speech: {
      title: "The Great Question (Human Rights Begin at Home)",
      year: 1958,
      description:
        "Eleanor Roosevelt on where human rights become real—in the ordinary places of daily life.",
      fullText: `Where, after all, do universal human rights begin?

In small places, close to home—so close and so small that they cannot be seen on any maps of the world. Yet they are the world of the individual person; the neighborhood he lives in; the school or college he attends; the factory, farm, or office where he works.

Such are the places where every man, woman, and child seeks equal justice, equal opportunity, equal dignity without discrimination. Unless these rights have meaning there, they have little meaning anywhere.

Without concerted citizen action to uphold them close to home, we shall look in vain for progress in the larger world.

The destiny of human rights is in the hands of all our citizens in all our communities.`,
    },
  },
  {
    id: 42,
    slug: "patrick_henry",
    file: "henry",
    dict: {
      id: 42,
      name: "Patrick Henry",
      description:
        "American founding orator whose 'Give me liberty, or give me death!' galvanized the Revolution",
      oratorName: "Patrick Henry",
      wordCount: 16,
      category: "Founding Era",
      era: "American Revolution (1736-1799)",
      bio: "Patrick Henry was a Virginia lawyer, planter, and statesman whose oratory helped push the colonies toward independence. At the Second Virginia Convention in 1775 he delivered the speech ending with the immortal line that made liberty the non-negotiable price of life itself.",
      portraitUrl: "",
      primaryStyle: "Fiery republican urgency",
      voiceStyle: "Dramatic, prophetic, uncompromising",
      colorAccent: 10027008,
      sampleSpeech:
        "Is life so dear, or peace so sweet, as to be purchased at the price of chains and slavery? Forbid it, Almighty God! I know not what course others may take; but as for me, give me liberty, or give me death!",
      tags: ["Revolution", "Liberty", "Courage", "Founding"],
      themeCategories: ["democracy", "courage", "leadership", "legacy", "inspirational"],
      isActive: true,
    },
    words: [
      {
        word: "liberty",
        definition: "Freedom from oppressive restriction on one's way of life or political views.",
        example:
          "I know not what course others may take; but as for me, give me liberty, or give me death!",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["democracy", "courage", "legacy"],
      },
      {
        word: "death",
        definition: "The end of life; here, the alternative to living without freedom.",
        example:
          "I know not what course others may take; but as for me, give me liberty, or give me death!",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["courage", "legacy"],
      },
      {
        word: "chain",
        definition: "A restraint that binds; metaphorically, political bondage.",
        example:
          "Is life so dear, or peace so sweet, as to be purchased at the price of chains and slavery?",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["courage", "democracy", "humanities"],
      },
      {
        word: "slavery",
        definition: "The condition of being owned and controlled; here, political subjugation.",
        example:
          "Is life so dear, or peace so sweet, as to be purchased at the price of chains and slavery?",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["democracy", "courage", "humanities"],
      },
      {
        word: "illusion",
        definition: "A false idea or belief; a comforting untruth.",
        example:
          "Mr. President, it is natural to man to indulge in the illusions of hope. We are apt to shut our eyes against a painful truth.",
        partOfSpeech: "noun",
        complexity: "advanced",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["humanities", "courage"],
      },
      {
        word: "hope",
        definition: "A feeling of expectation and desire for a certain thing to happen.",
        example:
          "It is natural to man to indulge in the illusions of hope. We are apt to shut our eyes against a painful truth.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["inspirational", "humanities"],
      },
      {
        word: "solace",
        definition: "Comfort or consolation in a time of distress.",
        example:
          "I have but one lamp by which my feet are guided, and that is the lamp of experience. I know of no way of judging of the future but by the past—and false hope is no solace when the fleets and armies are already here.",
        partOfSpeech: "noun",
        complexity: "advanced",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["humanities", "courage"],
      },
      {
        word: "experience",
        definition: "Practical contact with facts or events that yields knowledge.",
        example:
          "I have but one lamp by which my feet are guided, and that is the lamp of experience.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["leadership", "humanities"],
      },
      {
        word: "lamp",
        definition: "A device that gives light; metaphorically, a guide for judgment.",
        example:
          "I have but one lamp by which my feet are guided, and that is the lamp of experience.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["humanities", "legacy"],
      },
      {
        word: "war",
        definition: "Armed conflict between nations or parties.",
        example:
          "The war is actually begun! The next gale that sweeps from the north will bring to our ears the clash of resounding arms!",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["courage", "leadership", "legacy"],
      },
      {
        word: "gale",
        definition: "A very strong wind; a sudden forceful event.",
        example:
          "The next gale that sweeps from the north will bring to our ears the clash of resounding arms!",
        partOfSpeech: "noun",
        complexity: "advanced",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["arts", "courage"],
      },
      {
        word: "supplicate",
        definition: "To ask or beg for something earnestly or humbly.",
        example:
          "We have petitioned; we have remonstrated; we have supplicated; we have prostrated ourselves before the throne.",
        partOfSpeech: "verb",
        complexity: "advanced",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["democracy", "humanities"],
      },
      {
        word: "petition",
        definition: "A formal written request appealing to authority.",
        example:
          "We have petitioned; we have remonstrated; we have supplicated; we have prostrated ourselves before the throne.",
        partOfSpeech: "verb",
        complexity: "intermediate",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["democracy", "leadership"],
      },
      {
        word: "vigilant",
        definition: "Keeping careful watch for possible danger or difficulties.",
        example:
          "Gentlemen may cry, Peace, Peace—but there is no peace. The war is actually begun, and the vigilant freeman must hear the arms already raised.",
        partOfSpeech: "adjective",
        complexity: "advanced",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["courage", "leadership", "democracy"],
      },
      {
        word: "course",
        definition: "A path of action chosen among alternatives.",
        example:
          "I know not what course others may take; but as for me, give me liberty, or give me death!",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["courage", "leadership", "legacy"],
      },
      {
        word: "peace",
        definition: "Freedom from disturbance; quiet without conflict.",
        example:
          "Gentlemen may cry, Peace, Peace—but there is no peace. The war is actually begun!",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church)",
        categories: ["courage", "democracy", "legacy"],
      },
    ],
    quotes: [
      {
        text: "I know not what course others may take; but as for me, give me liberty, or give me death!",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention (St. John's Church, Richmond)",
        year: 1775,
        context: "Closing appeal for armed resistance to British rule.",
      },
      {
        text: "Is life so dear, or peace so sweet, as to be purchased at the price of chains and slavery? Forbid it, Almighty God!",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention",
        year: 1775,
        context: "Rejecting security bought by submission.",
      },
      {
        text: "I have but one lamp by which my feet are guided, and that is the lamp of experience.",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention",
        year: 1775,
        context: "Judging the future by Britain's past conduct.",
      },
      {
        text: "Gentlemen may cry, Peace, Peace—but there is no peace. The war is actually begun!",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention",
        year: 1775,
        context: "Declaring that conflict had already started in fact.",
      },
      {
        text: "The battle, sir, is not to the strong alone; it is to the vigilant, the active, the brave.",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention",
        year: 1775,
        context: "Encouraging a smaller force through moral readiness.",
      },
      {
        text: "We are apt to shut our eyes against a painful truth, and listen to the song of that siren till she transforms us into beasts.",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention",
        year: 1775,
        context: "Warning against comforting self-deception.",
      },
      {
        text: "Three millions of people, armed in the holy cause of liberty, and in such a country as that which we possess, are invincible by any force which our enemy can send against us.",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention",
        year: 1775,
        context: "Claiming popular resolve as a military advantage.",
      },
      {
        text: "It is natural to man to indulge in the illusions of hope.",
        source: "Give Me Liberty or Give Me Death",
        speech: "Second Virginia Convention",
        year: 1775,
        context: "Opening the case against false optimism.",
      },
    ],
    speech: {
      title: "Give Me Liberty or Give Me Death",
      year: 1775,
      description:
        "Key passages from Patrick Henry's speech at the Second Virginia Convention in Richmond.",
      fullText: `Mr. President, it is natural to man to indulge in the illusions of hope. We are apt to shut our eyes against a painful truth, and listen to the song of that siren till she transforms us into beasts.

I have but one lamp by which my feet are guided, and that is the lamp of experience. I know of no way of judging of the future but by the past. And judging by the past, I wish to know what there has been in the conduct of the British ministry for the last ten years to justify those hopes with which gentlemen have been pleased to solace themselves and the House.

Are fleets and armies necessary to a work of love and reconciliation? Have we shown ourselves so unwilling to be reconciled that force must be called in to win back our love?

We have petitioned; we have remonstrated; we have supplicated; we have prostrated ourselves before the throne, and have implored its interposition to arrest the tyrannical hands of the ministry and Parliament. Our petitions have been slighted; our remonstrances have produced additional violence and insult; our supplications have been disregarded; and we have been spurned, with contempt, from the foot of the throne!

In vain, after these things, may we indulge the fond hope of peace and reconciliation. There is no longer any room for hope. If we wish to be free—if we mean to preserve inviolate those inestimable privileges for which we have been so long contending—we must fight! I repeat it, sir, we must fight!

Gentlemen may cry, Peace, Peace—but there is no peace. The war is actually begun! The next gale that sweeps from the north will bring to our ears the clash of resounding arms! Our brethren are already in the field! Why stand we here idle?

Is life so dear, or peace so sweet, as to be purchased at the price of chains and slavery? Forbid it, Almighty God! I know not what course others may take; but as for me, give me liberty, or give me death!`,
    },
  },
  {
    id: 43,
    slug: "ruth_bader_ginsburg",
    file: "rbg",
    dict: {
      id: 43,
      name: "Ruth Bader Ginsburg",
      description:
        "U.S. Supreme Court Justice whose precise, relentless advocacy advanced equal citizenship under law",
      oratorName: "Ruth Bader Ginsburg",
      wordCount: 16,
      category: "Modern / Contemporary",
      era: "Late 20th–21st Century (1933-2020)",
      bio: "Ruth Bader Ginsburg was an American lawyer and jurist who served as an Associate Justice of the Supreme Court. Before the Court she built landmark equal-protection cases; on the Court she became known for careful opinions, powerful dissents, and a lifelong insistence that law treat women and men as full citizens.",
      portraitUrl: "",
      primaryStyle: "Precise legal moral force",
      voiceStyle: "Measured, exact, quietly fierce",
      colorAccent: 3359824,
      sampleSpeech:
        "I ask no favor for my sex. All I ask of our brethren is that they take their feet off our necks.",
      tags: ["Law", "Equality", "Dissent", "Citizenship"],
      themeCategories: ["democracy", "leadership", "humanities", "courage", "legacy"],
      isActive: true,
    },
    words: [
      {
        word: "dissent",
        definition: "A judicial opinion disagreeing with the majority; public disagreement in principle.",
        example:
          "Dissents speak to a future age. It's not simply to say, 'My colleagues are wrong and I would do it this way,' but the greatest dissents do become court opinions.",
        partOfSpeech: "noun",
        complexity: "advanced",
        source: "Interviews on the role of dissent",
        speech: "On dissent",
        categories: ["democracy", "leadership", "legacy"],
      },
      {
        word: "equality",
        definition: "The state of being equal, especially in status, rights, and opportunities.",
        example:
          "Women will only have true equality when men share with them the responsibility of bringing up the next generation.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Speeches on gender and law",
        speech: "On equality and family",
        categories: ["democracy", "humanities", "legacy"],
      },
      {
        word: "citizen",
        definition: "A person with full membership and standing under a constitution.",
        example:
          "The pedestal upon which women have been placed has all too often, upon closer inspection, been a cage—and full citizenship means stepping off both.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Equal-protection advocacy",
        speech: "On full citizenship",
        categories: ["democracy", "humanities"],
      },
      {
        word: "pedestal",
        definition: "A base supporting a statue; metaphorically, false elevation that restricts.",
        example:
          "The pedestal upon which women have been placed has all too often, upon closer inspection, been a cage.",
        partOfSpeech: "noun",
        complexity: "advanced",
        source: "Equal-protection advocacy",
        speech: "On full citizenship",
        categories: ["humanities", "democracy", "arts"],
      },
      {
        word: "cage",
        definition: "An enclosure that confines; a structure that looks protective but traps.",
        example:
          "The pedestal upon which women have been placed has all too often, upon closer inspection, been a cage.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Equal-protection advocacy",
        speech: "On full citizenship",
        categories: ["humanities", "courage"],
      },
      {
        word: "favor",
        definition: "Preferential treatment; an unearned advantage.",
        example:
          "I ask no favor for my sex. All I ask of our brethren is that they take their feet off our necks.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Quoting Sarah Grimké in advocacy and later remarks",
        speech: "On equal justice",
        categories: ["democracy", "courage", "legacy"],
      },
      {
        word: "brethren",
        definition: "Fellow members of a group; here, men as co-citizens.",
        example:
          "I ask no favor for my sex. All I ask of our brethren is that they take their feet off our necks.",
        partOfSpeech: "noun",
        complexity: "advanced",
        source: "Quoting Sarah Grimké in advocacy and later remarks",
        speech: "On equal justice",
        categories: ["democracy", "humanities"],
      },
      {
        word: "justice",
        definition: "The quality of being fair and reasonable under law.",
        example:
          "Fight for the things that you care about, but do it in a way that will lead others to join you—that is how justice gains allies.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Advice to young people",
        speech: "On fighting for justice",
        categories: ["democracy", "leadership", "inspirational"],
      },
      {
        word: "ally",
        definition: "A person or group that cooperates with another for a common purpose.",
        example:
          "Fight for the things that you care about, but do it in a way that will lead others to join you as an ally.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Advice to young people",
        speech: "On fighting for justice",
        categories: ["leadership", "inspirational"],
      },
      {
        word: "constitution",
        definition: "The fundamental principles and laws of a nation.",
        example:
          "The Constitution is a living document to the extent that we read its grand principles in light of new understandings—without discarding its text.",
        partOfSpeech: "noun",
        complexity: "advanced",
        source: "Judicial philosophy remarks",
        speech: "On the Constitution",
        categories: ["democracy", "humanities", "legacy"],
      },
      {
        word: "opinion",
        definition: "A formal statement of reasons for a judicial decision.",
        example:
          "The greatest dissents do become court opinions and gradually over time their views become the dominant view.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "On dissent",
        speech: "On dissent",
        categories: ["democracy", "leadership", "legacy"],
      },
      {
        word: "patience",
        definition: "The capacity to accept delay without anger while still pressing forward.",
        example:
          "Real change, enduring change, happens one step at a time—and patience is not the enemy of urgency.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Reflections on reform",
        speech: "On enduring change",
        categories: ["leadership", "inspirational", "legacy"],
      },
      {
        word: "enduring",
        definition: "Lasting over a long period; able to withstand strain.",
        example:
          "Real change, enduring change, happens one step at a time.",
        partOfSpeech: "adjective",
        complexity: "intermediate",
        source: "Reflections on reform",
        speech: "On enduring change",
        categories: ["legacy", "leadership"],
      },
      {
        word: "collegial",
        definition: "Relating to shared responsibility among colleagues; cooperative in tone.",
        example:
          "You can disagree without being disagreeable—a collegial court still argues hard about the law.",
        partOfSpeech: "adjective",
        complexity: "advanced",
        source: "On judicial culture",
        speech: "On disagreement",
        categories: ["leadership", "humanities"],
      },
      {
        word: "sex",
        definition: "Biological category of male or female; a basis of unjust legal classification.",
        example:
          "I ask no favor for my sex. All I ask of our brethren is that they take their feet off our necks.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Quoting Sarah Grimké",
        speech: "On equal justice",
        categories: ["democracy", "humanities", "courage"],
      },
      {
        word: "step",
        definition: "A single movement in a process of progress.",
        example:
          "Real change, enduring change, happens one step at a time.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Reflections on reform",
        speech: "On enduring change",
        categories: ["inspirational", "leadership"],
      },
    ],
    quotes: [
      {
        text: "I ask no favor for my sex. All I ask of our brethren is that they take their feet off our necks.",
        source: "Quoting Sarah Grimké in advocacy and public remarks",
        speech: "On equal justice",
        year: 2010,
        context: "Demanding removal of barriers, not special privilege.",
      },
      {
        text: "Fight for the things that you care about, but do it in a way that will lead others to join you.",
        source: "Advice to students and audiences",
        speech: "On effective advocacy",
        year: 2015,
        context: "Pairing conviction with coalition-building.",
      },
      {
        text: "Dissents speak to a future age.",
        source: "Interviews on judicial craft",
        speech: "On dissent",
        year: 2012,
        context: "Seeing minority opinions as messages to later courts.",
      },
      {
        text: "Women will only have true equality when men share with them the responsibility of bringing up the next generation.",
        source: "Speeches on family and equality",
        speech: "On shared responsibility",
        year: 2001,
        context: "Linking workplace equality to home life.",
      },
      {
        text: "Real change, enduring change, happens one step at a time.",
        source: "Reflections on law reform",
        speech: "On incremental progress",
        year: 2000,
        context: "Defending patient, cumulative legal strategy.",
      },
      {
        text: "You can disagree without being disagreeable.",
        source: "Remarks on collegiality",
        speech: "On disagreement",
        year: 2014,
        context: "Modeling firm principle with personal respect.",
      },
      {
        text: "When I'm sometimes asked when will there be enough women on the Supreme Court, and I say when there are nine, people are shocked. But there'd been nine men, and nobody's ever raised a question about that.",
        source: "Public interviews",
        speech: "On representation",
        year: 2012,
        context: "Challenging assumptions about who belongs on the Court.",
      },
      {
        text: "Reacting in anger or annoyance will not advance one's ability to persuade.",
        source: "Advice on advocacy",
        speech: "On persuasion",
        year: 2009,
        context: "Keeping temper in service of argument.",
      },
    ],
    speech: {
      title: "On Dissent, Equality, and Enduring Change",
      year: 2015,
      description:
        "A composite educational monologue drawn from Ruth Bader Ginsburg's public themes on law, dissent, and equality.",
      fullText: `I ask no favor for my sex. All I ask of our brethren is that they take their feet off our necks.

That sentence is older than my career, and it still describes the work: not privilege, but the removal of barriers that keep people from full citizenship.

Dissents speak to a future age. It is not simply to say, "My colleagues are wrong and I would do it this way." The greatest dissents do become court opinions, and gradually over time their views become the dominant view. So that's the dissenter's hope: that they are writing not for today, but for tomorrow.

Fight for the things that you care about, but do it in a way that will lead others to join you. Reacting in anger or annoyance will not advance one's ability to persuade.

Real change, enduring change, happens one step at a time. The law moves by argument, by record, by patience that is not the enemy of urgency.

You can disagree without being disagreeable. A court—and a country—needs both firm principle and the habit of listening.

Women will only have true equality when men share with them the responsibility of bringing up the next generation. Equality that stops at the office door is not equality at all.`,
    },
  },
  {
    id: 44,
    slug: "john_lewis",
    file: "lewis",
    dict: {
      id: 44,
      name: "John Lewis",
      description:
        "Civil rights leader and congressman who taught a generation to get in 'good trouble, necessary trouble'",
      oratorName: "John Lewis",
      wordCount: 16,
      category: "Modern / Contemporary",
      era: "Civil Rights to 21st Century (1940-2020)",
      bio: "John Lewis was a civil rights leader, chairman of SNCC, and long-serving U.S. Representative from Georgia. Beaten on the Edmund Pettus Bridge on Bloody Sunday, he spent a lifetime turning nonviolent courage into legislation and a moral vocabulary of good trouble.",
      portraitUrl: "",
      primaryStyle: "Moral urgency with disciplined nonviolence",
      voiceStyle: "Gentle, prophetic, unyielding",
      colorAccent: 9127187,
      sampleSpeech:
        "Never, ever be afraid to make some noise and get in good trouble, necessary trouble.",
      tags: ["Civil rights", "Nonviolence", "Democracy", "Courage"],
      themeCategories: ["courage", "democracy", "inspirational", "leadership", "legacy"],
      isActive: true,
    },
    words: [
      {
        word: "trouble",
        definition: "Difficulty or public disturbance; here, righteous disruption for justice.",
        example:
          "Never, ever be afraid to make some noise and get in good trouble, necessary trouble.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Commencement and rally addresses",
        speech: "Good Trouble",
        categories: ["courage", "democracy", "inspirational"],
      },
      {
        word: "necessary",
        definition: "Required to be done; essential to a just outcome.",
        example:
          "Never, ever be afraid to make some noise and get in good trouble, necessary trouble.",
        partOfSpeech: "adjective",
        complexity: "intermediate",
        source: "Commencement and rally addresses",
        speech: "Good Trouble",
        categories: ["courage", "democracy", "leadership"],
      },
      {
        word: "bridge",
        definition: "A structure spanning a gap; metaphorically, a crossing into risk for justice.",
        example:
          "We are going to march. We are going to walk. We are going across that bridge—and the nation will see what happens when peaceful people meet the club.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Selma / Bloody Sunday remembrances",
        speech: "On the Edmund Pettus Bridge",
        categories: ["courage", "legacy", "democracy"],
      },
      {
        word: "march",
        definition: "To walk together in a public demonstration for a cause.",
        example:
          "We are going to march. We are going to walk. We are going across that bridge.",
        partOfSpeech: "verb",
        complexity: "basic",
        source: "Selma campaign",
        speech: "On the Edmund Pettus Bridge",
        categories: ["courage", "democracy", "inspirational"],
      },
      {
        word: "nonviolence",
        definition: "The use of peaceful means, not force, to bring about political change.",
        example:
          "Nonviolence is not a tactic for the soft; it is a disciplined force that can redeem the soul of America.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Civil rights training and speeches",
        speech: "On nonviolence",
        categories: ["courage", "humanities", "leadership"],
      },
      {
        word: "redeem",
        definition: "To compensate for faults or to restore moral worth.",
        example:
          "Nonviolence is not a tactic for the soft; it is a disciplined force that can redeem the soul of America.",
        partOfSpeech: "verb",
        complexity: "advanced",
        source: "Civil rights speeches",
        speech: "On nonviolence",
        categories: ["inspirational", "humanities", "legacy"],
      },
      {
        word: "vote",
        definition: "A formal indication of choice in an election or decision.",
        example:
          "The vote is precious. It is almost sacred. It is the most powerful nonviolent tool we have in a democratic society.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Voting rights addresses",
        speech: "On the precious vote",
        categories: ["democracy", "leadership", "legacy"],
      },
      {
        word: "sacred",
        definition: "Connected with reverence; set apart as worthy of deep respect.",
        example:
          "The vote is precious. It is almost sacred. It is the most powerful nonviolent tool we have in a democratic society.",
        partOfSpeech: "adjective",
        complexity: "intermediate",
        source: "Voting rights addresses",
        speech: "On the precious vote",
        categories: ["democracy", "humanities"],
      },
      {
        word: "beloved",
        definition: "Dearly loved; forming a community bound by care.",
        example:
          "We must build a beloved community—not a utopia without conflict, but a place where justice is ordinary.",
        partOfSpeech: "adjective",
        complexity: "intermediate",
        source: "Civil rights philosophy",
        speech: "Beloved community",
        categories: ["inspirational", "humanities", "democracy"],
      },
      {
        word: "community",
        definition: "A group of people living in the same place or sharing a common purpose.",
        example:
          "We must build a beloved community—not a utopia without conflict, but a place where justice is ordinary.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Civil rights philosophy",
        speech: "Beloved community",
        categories: ["democracy", "inspirational", "leadership"],
      },
      {
        word: "justice",
        definition: "Just behavior or treatment; fairness as a public good.",
        example:
          "We must build a beloved community—not a utopia without conflict, but a place where justice is ordinary.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Civil rights philosophy",
        speech: "Beloved community",
        categories: ["democracy", "courage", "legacy"],
      },
      {
        word: "noise",
        definition: "Loud sound; public outcry that refuses silence.",
        example:
          "Never, ever be afraid to make some noise and get in good trouble, necessary trouble.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Commencement addresses",
        speech: "Good Trouble",
        categories: ["courage", "inspirational", "democracy"],
      },
      {
        word: "ordinary",
        definition: "With no special rank; common people as agents of history.",
        example:
          "Ordinary people with extraordinary vision can redeem the soul of America when they refuse to get used to injustice.",
        partOfSpeech: "adjective",
        complexity: "basic",
        source: "Final essay and public messages",
        speech: "On ordinary people",
        categories: ["inspirational", "democracy", "legacy"],
      },
      {
        word: "vision",
        definition: "The ability to think about or plan the future with imagination.",
        example:
          "Ordinary people with extraordinary vision can redeem the soul of America.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Final essay and public messages",
        speech: "On ordinary people",
        categories: ["inspirational", "leadership", "legacy"],
      },
      {
        word: "silence",
        definition: "Absence of sound or protest; quiet that can enable injustice.",
        example:
          "When you see something that is not right, not fair, not just, you have a moral obligation to say something, to do something—silence is not neutral.",
        partOfSpeech: "noun",
        complexity: "basic",
        source: "Moral obligation speeches",
        speech: "On moral obligation",
        categories: ["courage", "democracy", "humanities"],
      },
      {
        word: "obligation",
        definition: "A duty arising from moral principle or law.",
        example:
          "When you see something that is not right, not fair, not just, you have a moral obligation to say something, to do something.",
        partOfSpeech: "noun",
        complexity: "intermediate",
        source: "Moral obligation speeches",
        speech: "On moral obligation",
        categories: ["courage", "leadership", "democracy"],
      },
    ],
    quotes: [
      {
        text: "Never, ever be afraid to make some noise and get in good trouble, necessary trouble.",
        source: "Commencement and public addresses",
        speech: "Good Trouble",
        year: 2016,
        context: "Urging young people toward righteous civic disruption.",
      },
      {
        text: "The vote is precious. It is almost sacred. It is the most powerful nonviolent tool we have in a democratic society.",
        source: "Voting rights addresses",
        speech: "On the precious vote",
        year: 2008,
        context: "Elevating suffrage as nonviolent power.",
      },
      {
        text: "When you see something that is not right, not fair, not just, you have a moral obligation to say something, to do something.",
        source: "Public moral appeals",
        speech: "On moral obligation",
        year: 2018,
        context: "Linking witnessing injustice to action.",
      },
      {
        text: "Ordinary people with extraordinary vision can redeem the soul of America.",
        source: "Final New York Times essay themes / public messages",
        speech: "On ordinary people",
        year: 2020,
        context: "Placing national redemption in citizen hands.",
      },
      {
        text: "We may not have chosen the time, but the time has chosen us.",
        source: "Civil rights and later addresses",
        speech: "On being chosen by the moment",
        year: 1965,
        context: "Accepting historical responsibility.",
      },
      {
        text: "You must be bold, brave, and courageous and find a way to get in the way.",
        source: "Rallies and commencements",
        speech: "Get in the way",
        year: 2014,
        context: "Calling students into active obstruction of injustice.",
      },
      {
        text: "Freedom is not a state; it is an act. It is not some enchanted garden perched high on a distant plateau where we can finally sit down and rest.",
        source: "Across That Bridge / related teachings",
        speech: "On freedom as action",
        year: 2012,
        context: "Defining freedom as continual practice.",
      },
      {
        text: "Release the need to hate, to harbor division, and the enticement of revenge. Make room for both justice and grace.",
        source: "Final public message themes",
        speech: "On justice and grace",
        year: 2020,
        context: "Urging a nation past vengeance toward repair.",
      },
    ],
    speech: {
      title: "Good Trouble, Necessary Trouble",
      year: 2018,
      description:
        "An educational monologue gathering John Lewis's signature themes of nonviolence, voting, and good trouble.",
      fullText: `Never, ever be afraid to make some noise and get in good trouble, necessary trouble.

When you see something that is not right, not fair, not just, you have a moral obligation to say something, to do something. Silence is not neutral. Silence is a choice.

The vote is precious. It is almost sacred. It is the most powerful nonviolent tool we have in a democratic society. People died for the right of all of us to participate. We honor them not only with memory, but with ballots and with our bodies when the path is blocked.

I was beaten on a bridge in Selma, and I never hated the men who held the clubs. Nonviolence is not a tactic for the soft; it is a disciplined force that can redeem the soul of America.

You must be bold, brave, and courageous and find a way to get in the way. Ordinary people with extraordinary vision can redeem the soul of America when they refuse to get used to injustice.

Freedom is not a state; it is an act. It is not some enchanted garden where we finally sit down and rest. We walk, we march, we cross bridges again and again—until justice is ordinary and the beloved community is more than a dream.`,
    },
  },
];

function wordId(oratorId, index) {
  // Avoid the crowded 40000–40368 band used by classic orators.
  if (oratorId === 40) return 40400 + index;
  return oratorId * 1000 + index;
}

function quoteId(oratorId, index) {
  return oratorId * 1000 + index;
}

function writeJson(filePath, data) {
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2) + "\n");
}

// --- dictionaries ---
const dictPath = path.join(seed, "dictionaries.json");
let dictionaries = JSON.parse(fs.readFileSync(dictPath, "utf8"));
dictionaries = dictionaries.filter((d) => d.id < 39 || d.id > 44);
for (const o of NEW_ORATORS) dictionaries.push(o.dict);
dictionaries.sort((a, b) => a.id - b.id);
writeJson(dictPath, dictionaries);

// --- speeches ---
const speechesPath = path.join(seed, "speeches.json");
let speeches = JSON.parse(fs.readFileSync(speechesPath, "utf8"));
speeches = speeches.filter((s) => s.oratorId < 39 || s.oratorId > 44);
let nextSpeechId = Math.max(0, ...speeches.map((s) => s.id)) + 1;
for (const o of NEW_ORATORS) {
  speeches.push({
    id: nextSpeechId++,
    oratorId: o.id,
    title: o.speech.title,
    fullText: o.speech.fullText,
    year: o.speech.year,
    description: o.speech.description,
  });
}
writeJson(speechesPath, speeches);

// --- words + quotes files ---
for (const o of NEW_ORATORS) {
  const words = o.words.map((w, i) => ({
    id: wordId(o.id, i + 1),
    oratorId: o.id,
    ...w,
  }));
  writeJson(path.join(seed, `words_${o.file}.json`), words);

  const quotes = o.quotes.map((q, i) => ({
    id: quoteId(o.id, i + 1),
    oratorId: o.id,
    ...q,
  }));
  writeJson(path.join(seed, `quotes_${o.file}.json`), quotes);
}

// --- OratorPortraits.kt ---
let portraits = fs.readFileSync(portraitsPath, "utf8");
if (!portraits.includes('39L to "theodore_roosevelt"')) {
  portraits = portraits.replace(
    /38L to "tommy_conlon",\s*\n\s*\)/,
    `38L to "tommy_conlon",
        39L to "theodore_roosevelt",
        40L to "ronald_reagan",
        41L to "eleanor_roosevelt",
        42L to "patrick_henry",
        43L to "ruth_bader_ginsburg",
        44L to "john_lewis",
    )`,
  );
  fs.writeFileSync(portraitsPath, portraits);
}

// --- SEED_VERSION ---
let loader = fs.readFileSync(loaderPath, "utf8");
loader = loader.replace(
  /const val SEED_VERSION = \d+/,
  "const val SEED_VERSION = 6",
);
fs.writeFileSync(loaderPath, loader);

// --- validation ---
const themes = new Set([
  "inspirational",
  "tech",
  "humanities",
  "arts",
  "leadership",
  "democracy",
  "courage",
  "legacy",
]);
const wordIds = new Set();
const failures = [];
for (const f of fs.readdirSync(seed).filter((x) => x.startsWith("words_"))) {
  for (const w of JSON.parse(fs.readFileSync(path.join(seed, f), "utf8"))) {
    if (wordIds.has(w.id)) failures.push(`duplicate word id ${w.id} in ${f}`);
    wordIds.add(w.id);
    const lw = String(w.word).toLowerCase();
    const le = String(w.example || "").toLowerCase();
    const ok =
      le.includes(lw) ||
      le.includes(lw + "s") ||
      le.includes(lw + "d") ||
      le.includes(lw + "ing") ||
      le.includes(lw + "ed");
    if (!ok) failures.push(`${f}: '${w.word}' missing from example`);
    for (const c of w.categories || []) {
      if (!themes.has(c)) failures.push(`${f}: invalid theme ${c}`);
    }
  }
}

console.log(
  `Added ${NEW_ORATORS.length} orators; dictionaries=${dictionaries.length}; speeches=${speeches.length}`,
);
if (failures.length) {
  console.error("VALIDATION FAILURES:");
  failures.forEach((x) => console.error(" ", x));
  process.exit(1);
}
console.log("Validation OK");
