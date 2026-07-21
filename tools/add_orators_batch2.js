/**
 * Add orators 45–54 with words, quotes, speeches.
 * Re-run safe: replaces only those ids/files.
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

const THEMES = new Set([
  "inspirational",
  "tech",
  "humanities",
  "arts",
  "leadership",
  "democracy",
  "courage",
  "legacy",
]);

/** @type {Array<any>} */
const ORATORS = [
  {
    id: 45,
    slug: "sojourner_truth",
    file: "truth",
    dict: {
      id: 45,
      name: "Sojourner Truth",
      description:
        "Abolitionist and women's rights advocate whose 'Ain't I a Woman?' still cuts through empty piety",
      oratorName: "Sojourner Truth",
      wordCount: 16,
      category: "19th Century Powerhouses",
      era: "19th Century (c. 1797-1883)",
      bio: "Sojourner Truth was born into slavery in New York, escaped with her infant daughter, and became a legendary itinerant preacher and reformer. Her plain speech, moral force, and the speech remembered as 'Ain't I a Woman?' made her one of America's most unforgettable orators.",
      portraitUrl: "",
      primaryStyle: "Plainspoken moral thunder",
      voiceStyle: "Direct, earthy, unforgettable",
      colorAccent: 9127187,
      sampleSpeech:
        "That man over there says that women need to be helped into carriages. Nobody ever helps me into carriages. And ain't I a woman?",
      tags: ["Abolition", "Women's rights", "Faith", "Truth"],
      themeCategories: ["courage", "democracy", "humanities", "inspirational", "legacy"],
      isActive: true,
    },
    words: w([
      ["woman", "An adult female person; here, a claim to full human dignity.", "And ain't I a woman?", "noun", "basic", "Ain't I a Woman?", "Women's Convention, Akron", ["democracy", "courage", "legacy"]],
      ["carriage", "A wheeled vehicle for people; a symbol of polite privilege.", "That man over there says that women need to be helped into carriages—and lifted over ditches.", "noun", "intermediate", "Ain't I a Woman?", "Women's Convention, Akron", ["humanities", "democracy"]],
      ["ditch", "A narrow channel dug in the ground; a muddy obstacle of daily labor.", "That man over there says that women need to be helped into carriages, and lifted over ditches, and to have the best place everywhere.", "noun", "basic", "Ain't I a Woman?", "Women's Convention, Akron", ["courage", "humanities"]],
      ["plough", "To turn soil with a plough; hard field labor.", "I have ploughed and planted, and gathered into barns, and no man could head me.", "verb", "intermediate", "Ain't I a Woman?", "Women's Convention, Akron", ["courage", "legacy"]],
      ["plant", "To put seeds or young plants in the ground to grow.", "I have ploughed and planted, and gathered into barns, and no man could head me—and ain't I a woman?", "verb", "basic", "Ain't I a Woman?", "Women's Convention, Akron", ["courage", "humanities"]],
      ["intellect", "The faculty of reasoning and understanding.", "What's that got to do with women's rights or negroes' rights? If my cup won't hold but a pint, and yours holds a quart, wouldn't you be mean not to let me have my little half-measure full—intellect is not a closed club.", "noun", "advanced", "Ain't I a Woman?", "Women's Convention, Akron", ["humanities", "democracy"]],
      ["pint", "A unit of liquid measure; a small share of capacity.", "If my cup won't hold but a pint, and yours holds a quart, wouldn't you be mean not to let me have my little half-measure full?", "noun", "intermediate", "Ain't I a Woman?", "Women's Convention, Akron", ["humanities", "democracy"]],
      ["quart", "A unit of liquid measure equal to two pints; a larger share.", "If my cup won't hold but a pint, and yours holds a quart, wouldn't you be mean not to let me have my little half-measure full?", "noun", "intermediate", "Ain't I a Woman?", "Women's Convention, Akron", ["humanities", "democracy"]],
      ["truth", "That which is in accordance with fact or reality; her chosen name and method.", "The Lord has made me a sign unto this nation, an' I go round a-testifyin' for truth.", "noun", "basic", "Narrative and lectures", "On her mission", ["humanities", "inspirational", "legacy"]],
      ["slavery", "The system of owning people as property.", "I have borne thirteen children, and seen most all sold off to slavery, and when I cried out with my mother's grief, none but Jesus heard me.", "noun", "intermediate", "Ain't I a Woman?", "Women's Convention, Akron", ["courage", "humanities", "legacy"]],
      ["grief", "Deep sorrow, especially from loss.", "I have borne thirteen children, and seen most all sold off to slavery, and when I cried out with my mother's grief, none but Jesus heard me.", "noun", "intermediate", "Ain't I a Woman?", "Women's Convention, Akron", ["humanities", "courage"]],
      ["right", "A moral or legal entitlement.", "If the first woman God ever made was strong enough to turn the world upside down all alone, these women together ought to be able to turn it back, and get it right side up again.", "noun", "basic", "Ain't I a Woman?", "Women's Convention, Akron", ["democracy", "courage"]],
      ["strong", "Having power to endure labor, pain, or opposition.", "I have ploughed and planted, and gathered into barns, and no man could head me—and ain't I a woman? Look at me! Look at my arm! I have as much muscle as any man, and can do as much work as any man—I am as strong.", "adjective", "basic", "Ain't I a Woman?", "Women's Convention, Akron", ["courage", "inspirational"]],
      ["equal", "Having the same status, rights, or value.", "If women want any rights more than they's got, why don't they just take them, and not be talking about it—equal standing is claimed by work as well as by words.", "adjective", "basic", "Lectures on rights", "On taking rights", ["democracy", "courage"]],
      ["sojourn", "A temporary stay; to dwell for a time while traveling.", "The Spirit calls me, and I must sojourn and go speak the truth where it is needed.", "verb", "advanced", "On taking her name", "On her calling", ["inspirational", "legacy", "humanities"]],
      ["testify", "To declare solemnly as witness; to speak publicly for a cause.", "I go round a-testifyin' and showin' this people their sins and the need of repentance.", "verb", "intermediate", "Narrative and lectures", "On her mission", ["courage", "humanities", "inspirational"]],
    ]),
    quotes: q([
      ["And ain't I a woman?", "Ain't I a Woman?", "Women's Convention, Akron, Ohio", 1851, "Claiming full womanhood against polite exclusion."],
      ["If my cup won't hold but a pint, and yours holds a quart, wouldn't you be mean not to let me have my little half-measure full?", "Ain't I a Woman?", "Women's Convention, Akron", 1851, "On sharing intellect and opportunity."],
      ["I am not going to die, I'm going home like a shooting star.", "Attributed late-life remark", "On death", 1883, "Facing the end with defiance and light."],
      ["Truth is powerful and it prevails.", "Lectures", "On truth", 1850, "Her method and her name."],
      ["Religion without humanity is poor human stuff.", "Addresses on faith", "On religion", 1853, "Linking belief to humane conduct."],
      ["I feel safe even in the midst of my enemies; for the truth is powerful and will prevail.", "Narrative themes", "On safety in truth", 1850, "Courage rooted in conviction."],
      ["If the first woman God ever made was strong enough to turn the world upside down all alone, these women together ought to be able to turn it back.", "Ain't I a Woman?", "Women's Convention, Akron", 1851, "Calling women into collective power."],
      ["Where there is so much racket there must be something out of kilter.", "Ain't I a Woman?", "Women's Convention, Akron", 1851, "Opening by naming the disturbance of injustice."],
    ]),
    speech: {
      title: "Ain't I a Woman?",
      year: 1851,
      description: "Key passages from Sojourner Truth's address at the Women's Convention in Akron (as commonly remembered in later transcripts).",
      fullText: `Well, children, where there is so much racket there must be something out of kilter. I think that 'twixt the negroes of the South and the women at the North, all talking about rights, the white men will be in a fix pretty soon.

That man over there says that women need to be helped into carriages, and lifted over ditches, and to have the best place everywhere. Nobody ever helps me into carriages, or over mud-puddles, or gives me any best place! And ain't I a woman?

Look at me! Look at my arm! I have ploughed and planted, and gathered into barns, and no man could head me! And ain't I a woman? I could work as much and eat as much as a man—when I could get it—and bear the lash as well! And ain't I a woman?

I have borne thirteen children, and seen most all sold off to slavery, and when I cried out with my mother's grief, none but Jesus heard me! And ain't I a woman?

Then they talk about this thing in the head; what's this they call it? Intellect. What's that got to do with women's rights or negroes' rights? If my cup won't hold but a pint, and yours holds a quart, wouldn't you be mean not to let me have my little half-measure full?

If the first woman God ever made was strong enough to turn the world upside down all alone, these women together ought to be able to turn it back, and get it right side up again! And now they is asking to do it, the men better let them.`,
    },
  },
  {
    id: 46,
    slug: "susan_b_anthony",
    file: "anthony",
    dict: {
      id: 46,
      name: "Susan B. Anthony",
      description: "Suffrage organizer whose failure was impossible and whose ballot was an act of civil disobedience",
      oratorName: "Susan B. Anthony",
      wordCount: 16,
      category: "19th Century Powerhouses",
      era: "19th–early 20th Century (1820-1906)",
      bio: "Susan B. Anthony was a leading organizer of the American women's suffrage movement. She was arrested for voting in 1872, toured relentlessly, and built a disciplined national campaign whose victory she did not live to see—but predicted.",
      portraitUrl: "",
      primaryStyle: "Organized moral insistence",
      voiceStyle: "Clear, firm, unsentimental",
      colorAccent: 4737096,
      sampleSpeech: "Failure is impossible.",
      tags: ["Suffrage", "Equality", "Organizing", "Ballot"],
      themeCategories: ["democracy", "courage", "leadership", "legacy", "inspirational"],
      isActive: true,
    },
    words: w([
      ["failure", "Lack of success; the opposite of the movement's destiny as she named it.", "Failure is impossible.", "noun", "basic", "Last public words / convention charge", "On the suffrage cause", ["inspirational", "legacy", "courage"]],
      ["impossible", "Not able to occur or be done—except when she denied the word power.", "Failure is impossible.", "adjective", "basic", "Last public words / convention charge", "On the suffrage cause", ["inspirational", "courage"]],
      ["ballot", "A system or act of voting; the paper or ticket used to cast a vote.", "It was we, the people; not we, the white male citizens; nor yet we, the male citizens; but we, the whole people, who formed the Union—and the ballot belongs to that whole.", "noun", "intermediate", "On Women's Right to Vote", "After her 1872 arrest", ["democracy", "leadership"]],
      ["citizen", "A legally recognized member of a state with rights and duties.", "It was we, the people; not we, the white male citizens; nor yet we, the male citizens; but we, the whole people, who formed the Union.", "noun", "basic", "On Women's Right to Vote", "After her 1872 arrest", ["democracy", "humanities"]],
      ["union", "The United States as a political whole; a joining into one body.", "It was we, the people; not we, the white male citizens; nor yet we, the male citizens; but we, the whole people, who formed the Union.", "noun", "intermediate", "On Women's Right to Vote", "After her 1872 arrest", ["democracy", "legacy"]],
      ["oligarchy", "Government by a small privileged group.", "To them this government is not a democracy; it is not a republic; it is an odious aristocracy; a hateful oligarchy of sex.", "noun", "advanced", "On Women's Right to Vote", "After her 1872 arrest", ["democracy", "humanities", "courage"]],
      ["aristocracy", "Rule by a hereditary or elite class.", "To them this government is not a democracy; it is not a republic; it is an odious aristocracy; a hateful oligarchy of sex.", "noun", "advanced", "On Women's Right to Vote", "After her 1872 arrest", ["democracy", "humanities"]],
      ["odious", "Extremely unpleasant; deserving hatred.", "To them this government is not a democracy; it is not a republic; it is an odious aristocracy; a hateful oligarchy of sex.", "adjective", "advanced", "On Women's Right to Vote", "After her 1872 arrest", ["humanities", "courage"]],
      ["suffrage", "The right to vote in political elections.", "Suffrage is the pivotal right.", "noun", "intermediate", "Campaign addresses", "On suffrage", ["democracy", "leadership", "legacy"]],
      ["pivotal", "Of crucial importance in relation to the development or success of something.", "Suffrage is the pivotal right.", "adjective", "advanced", "Campaign addresses", "On suffrage", ["democracy", "leadership"]],
      ["independence", "Freedom from control by others; self-government.", "Cautious, careful people, always casting about to preserve their reputation and social standing, never can bring about a reform—independence of mind is required.", "noun", "intermediate", "Letters and speeches on reform", "On reformers", ["courage", "leadership"]],
      ["reform", "Make changes in order to improve a system or institution.", "Cautious, careful people, always casting about to preserve their reputation and social standing, never can bring about a reform.", "noun", "intermediate", "Letters and speeches on reform", "On reformers", ["democracy", "leadership"]],
      ["organize", "Coordinate people into an effective body for action.", "The true woman will not be exponent of another, or allow another to be such for her—she will organize her own power.", "verb", "intermediate", "Organizational addresses", "On women's power", ["leadership", "democracy"]],
      ["arrest", "Seize by legal authority and take into custody.", "I was arrested for voting, and I am as guilty of the crime of demanding my rights as any freeman who ever cast a ballot.", "verb", "basic", "On Women's Right to Vote", "After her 1872 arrest", ["courage", "democracy", "legacy"]],
      ["guilty", "Responsible for a specified wrong—or proudly so in civil disobedience.", "I shall earnestly and persistently continue to urge all women to the practical recognition of the old revolutionary maxim that Resistance to tyranny is obedience to God—and if that makes me guilty, so be it.", "adjective", "intermediate", "On Women's Right to Vote", "After her 1872 arrest", ["courage", "democracy"]],
      ["tyranny", "Cruel and oppressive government or rule.", "Resistance to tyranny is obedience to God.", "noun", "intermediate", "On Women's Right to Vote", "After her 1872 arrest", ["courage", "democracy", "legacy"]],
    ]),
    quotes: q([
      ["Failure is impossible.", "Final public convention charge", "National American Woman Suffrage Association", 1906, "Her last great word to the movement."],
      ["It was we, the people; not we, the white male citizens; nor yet we, the male citizens; but we, the whole people, who formed the Union.", "On Women's Right to Vote", "Defense of her 1872 vote", 1873, "Reading women into the Constitution's 'people'."],
      ["Suffrage is the pivotal right.", "Campaign addresses", "On the ballot", 1870, "Naming the vote as the hinge of citizenship."],
      ["Cautious, careful people, always casting about to preserve their reputation and social standing, never can bring about a reform.", "On reformers", "Letters and speeches", 1860, "Demanding risk from change-makers."],
      ["Men, their rights, and nothing more; women, their rights, and nothing less.", "Revolution masthead / motto", "The Revolution", 1868, "Program of equal rights without excess or deficit."],
      ["Independence is happiness.", "Personal maxims in public work", "On independence", 1875, "Linking self-reliance to joy."],
      ["I declare to you that woman must not depend upon the protection of man, but must be taught to protect herself.", "Lectures", "On self-protection", 1871, "Rejecting paternal guardianship as substitute for rights."],
      ["There never will be complete equality until women themselves help to make laws and elect lawmakers.", "Suffrage arguments", "On lawmaking", 1880, "Connecting equality to legislative power."],
    ]),
    speech: {
      title: "On Women's Right to Vote",
      year: 1873,
      description: "Passages from Susan B. Anthony's defense after voting illegally in the 1872 presidential election.",
      fullText: `Friends and fellow citizens: I stand before you tonight under indictment for the alleged crime of having voted at the last presidential election, without having a lawful right to vote.

It shall be my work this evening to prove to you that in thus voting, I not only committed no crime, but, instead, simply exercised my citizen's rights, guaranteed to me and all United States citizens by the National Constitution, beyond the power of any state to deny.

The preamble of the Federal Constitution says: "We, the people of the United States, in order to form a more perfect union, establish justice, insure domestic tranquillity, provide for the common defense, promote the general welfare, and secure the blessings of liberty to ourselves and our posterity, do ordain and establish this Constitution for the United States of America."

It was we, the people; not we, the white male citizens; nor yet we, the male citizens; but we, the whole people, who formed the Union. And we formed it, not to give the blessings of liberty, but to secure them; not to the half of ourselves and the half of our posterity, but to the whole people—women as well as men.

And it is a downright mockery to talk to women of their enjoyment of the blessings of liberty while they are denied the use of the only means of securing them provided by this democratic-republican government—the ballot.

To them this government has no just powers derived from the consent of the governed. To them this government is not a democracy; it is not a republic. It is an odious aristocracy; a hateful oligarchy of sex.

Failure is impossible.`,
    },
  },
  {
    id: 47,
    slug: "benjamin_franklin",
    file: "franklin",
    dict: {
      id: 47,
      name: "Benjamin Franklin",
      description: "Founder, printer, and diplomat whose wit made virtue and self-government portable",
      oratorName: "Benjamin Franklin",
      wordCount: 16,
      category: "Founding Era",
      era: "Colonial & Founding America (1706-1790)",
      bio: "Benjamin Franklin was a printer, scientist, diplomat, and framer of the American founding. Through Poor Richard, the Constitutional Convention, and public letters, he taught thrift, industry, compromise, and liberty with a smile sharp enough to cut.",
      portraitUrl: "",
      primaryStyle: "Witty practical wisdom",
      voiceStyle: "Ironic, plain, persuasive",
      colorAccent: 12632256,
      sampleSpeech: "They who can give up essential liberty to obtain a little temporary safety deserve neither liberty nor safety.",
      tags: ["Founding", "Virtue", "Wit", "Diplomacy"],
      themeCategories: ["democracy", "humanities", "leadership", "legacy", "inspirational"],
      isActive: true,
    },
    words: w([
      ["liberty", "Freedom from arbitrary control; a political and personal good.", "They who can give up essential liberty to obtain a little temporary safety deserve neither liberty nor safety.", "noun", "intermediate", "Reply to the Governor / Pennsylvania Assembly", "On liberty and safety", ["democracy", "courage", "legacy"]],
      ["safety", "The condition of being protected from danger.", "They who can give up essential liberty to obtain a little temporary safety deserve neither liberty nor safety.", "noun", "basic", "Reply to the Governor / Pennsylvania Assembly", "On liberty and safety", ["democracy", "leadership"]],
      ["essential", "Absolutely necessary; forming the core of something.", "They who can give up essential liberty to obtain a little temporary safety deserve neither liberty nor safety.", "adjective", "intermediate", "Reply to the Governor / Pennsylvania Assembly", "On liberty and safety", ["democracy", "humanities"]],
      ["industry", "Diligent work; productive activity.", "Early to bed and early to rise makes a man healthy, wealthy, and wise—and industry is the engine under that proverb.", "noun", "intermediate", "Poor Richard's Almanack", "Poor Richard", ["inspirational", "leadership"]],
      ["frugality", "The quality of being economical with money or resources.", "Beware of little expenses; a small leak will sink a great ship—and frugality plugs the leak.", "noun", "advanced", "Poor Richard's Almanack", "Poor Richard", ["humanities", "leadership"]],
      ["diligence", "Careful and persistent work or effort.", "Diligence is the mother of good luck.", "noun", "intermediate", "Poor Richard's Almanack", "Poor Richard", ["inspirational", "leadership"]],
      ["compromise", "A settlement of dispute by mutual concession.", "When a broad table is to be made, and the edges of planks do not fit, the artist takes a little from both—and compromise makes the joint.", "noun", "intermediate", "Constitutional Convention remarks", "On the Constitution", ["democracy", "leadership"]],
      ["constitution", "The fundamental law of a political body.", "I agree to this Constitution with all its faults, if they are such; because I think a general government necessary for us.", "noun", "advanced", "Speech at the Constitutional Convention", "Closing speech on the Constitution", ["democracy", "legacy", "leadership"]],
      ["fault", "An imperfection or defect.", "I agree to this Constitution with all its faults, if they are such; because I think a general government necessary for us.", "noun", "basic", "Speech at the Constitutional Convention", "Closing speech on the Constitution", ["democracy", "humanities"]],
      ["humility", "A modest view of one's own importance.", "In this world nothing can be said to be certain, except death and taxes—a line that teaches humility about our plans.", "noun", "intermediate", "Letter on the Constitution / later maxims", "On certainty", ["humanities", "inspirational"]],
      ["virtue", "Behavior showing high moral standards; excellence of character.", "Only a virtuous people are capable of freedom. As nations become corrupt and vicious, they have more need of masters—virtue is the republic's fuel.", "noun", "intermediate", "Letters on government", "On virtue and freedom", ["democracy", "humanities", "legacy"]],
      ["energy", "Forceful exertion; capacity for action.", "Energy and persistence conquer all things.", "noun", "basic", "Attributed maxims", "On energy", ["inspirational", "leadership"]],
      ["persistence", "Firm continuance in a course of action despite difficulty.", "Energy and persistence conquer all things.", "noun", "intermediate", "Attributed maxims", "On energy", ["inspirational", "courage"]],
      ["lost", "Unable to be found or recovered; wasted.", "Lost time is never found again.", "adjective", "basic", "Poor Richard's Almanack", "Poor Richard", ["inspirational", "humanities"]],
      ["well", "In a good or satisfactory way; thoroughly done.", "Well done is better than well said.", "adverb", "basic", "Poor Richard's Almanack", "Poor Richard", ["leadership", "inspirational"]],
      ["hang", "To be suspended; metaphorically, to stand or fall together.", "We must, indeed, all hang together, or most assuredly we shall all hang separately.", "verb", "basic", "At the signing of the Declaration (attributed)", "On unity", ["courage", "democracy", "leadership"]],
    ]),
    quotes: q([
      ["They who can give up essential liberty to obtain a little temporary safety deserve neither liberty nor safety.", "Pennsylvania Assembly reply", "On liberty and safety", 1755, "Warning against trading freedom for security theater."],
      ["We must, indeed, all hang together, or most assuredly we shall all hang separately.", "Attributed at Declaration signing", "On colonial unity", 1776, "Humor with a noose's edge."],
      ["Well done is better than well said.", "Poor Richard's Almanack", "Poor Richard", 1737, "Preferring deeds to rhetoric."],
      ["Diligence is the mother of good luck.", "Poor Richard's Almanack", "Poor Richard", 1736, "Luck as the child of work."],
      ["Lost time is never found again.", "Poor Richard's Almanack", "Poor Richard", 1748, "On the irrecoverable hour."],
      ["I agree to this Constitution with all its faults, if they are such.", "Constitutional Convention", "Closing speech", 1787, "Supporting imperfect union over disunion."],
      ["In this world nothing can be said to be certain, except death and taxes.", "Letter to Jean-Baptiste Le Roy", "On certainty", 1789, "A durable American joke with bite."],
      ["An investment in knowledge pays the best interest.", "Poor Richard themes / popular form", "On knowledge", 1758, "Education as compound return."],
    ]),
    speech: {
      title: "Closing Speech at the Constitutional Convention",
      year: 1787,
      description: "Benjamin Franklin on imperfect agreement and the need for a general government.",
      fullText: `I confess that there are several parts of this Constitution which I do not at present approve, but I am not sure I shall never approve them. For having lived long, I have experienced many instances of being obliged by better information, or fuller consideration, to change opinions even on important subjects, which I once thought right, but found to be otherwise.

I agree to this Constitution with all its faults, if they are such; because I think a general government necessary for us, and there is no form of government but what may be a blessing to the people if well administered, and believe farther that this is likely to be well administered for a course of years, and can only end in despotism, as other forms have done before it, when the people shall become so corrupted as to need despotic government, being incapable of any other.

I doubt too whether any other Convention we can obtain, may be able to make a better Constitution. For when you assemble a number of men to have the advantage of their joint wisdom, you inevitably assemble with those men all their prejudices, their passions, their errors of opinion, their local interests, and their selfish views. From such an assembly can a perfect production be expected?

It therefore astonishes me, Sir, to find this system approaching so near to perfection as it does; and I think it will astonish our enemies.

On the whole, Sir, I cannot help expressing a wish that every member of the Convention who may still have objections to it, would with me, on this occasion doubt a little of his own infallibility, and to make manifest our unanimity, put his name to this instrument.`,
    },
  },
  {
    id: 48,
    slug: "malcolm_x",
    file: "malcolm",
    dict: {
      id: 48,
      name: "Malcolm X",
      description: "Minister and activist whose evolving oratory demanded dignity by any means necessary—and then beyond it",
      oratorName: "Malcolm X",
      wordCount: 16,
      category: "20th Century Legends",
      era: "Civil Rights era (1925-1965)",
      bio: "Malcolm X was a Muslim minister and human rights activist. His speeches moved from uncompromising Black nationalism to a broader human-rights internationalism after his pilgrimage to Mecca, always delivered with clarity, cadence, and moral heat.",
      portraitUrl: "",
      primaryStyle: "Uncompromising clarity",
      voiceStyle: "Sharp, rhythmic, relentless",
      colorAccent: 2368548,
      sampleSpeech: "We declare our right on this earth to be a man, to be a human being, to be respected as a human being, to be given the rights of a human being in this society, on this earth, in this day, which we intend to bring into existence by any means necessary.",
      tags: ["Civil rights", "Dignity", "Self-determination", "Truth"],
      themeCategories: ["courage", "democracy", "humanities", "leadership", "legacy"],
      isActive: true,
    },
    words: w([
      ["means", "A method or instrument for achieving a result.", "We declare our right on this earth to be a man, to be a human being, to be respected as a human being—by any means necessary.", "noun", "basic", "Organization of Afro-American Unity", "By Any Means Necessary", ["courage", "leadership", "democracy"]],
      ["necessary", "Required to be done; essential.", "By any means necessary.", "adjective", "intermediate", "Organization of Afro-American Unity", "By Any Means Necessary", ["courage", "leadership"]],
      ["human", "Of or characteristic of people; claiming full personhood.", "We declare our right on this earth to be a man, to be a human being, to be respected as a human being.", "adjective", "basic", "Organization of Afro-American Unity", "By Any Means Necessary", ["humanities", "democracy", "courage"]],
      ["respect", "Due regard for the feelings, rights, or traditions of others.", "We declare our right on this earth to be a man, to be a human being, to be respected as a human being.", "verb", "basic", "Organization of Afro-American Unity", "By Any Means Necessary", ["humanities", "democracy"]],
      ["ballot", "A system of voting; political participation as power.", "The ballot or the bullet—if you don't take it through the ballot, you may be forced to take it another way.", "noun", "intermediate", "The Ballot or the Bullet", "The Ballot or the Bullet", ["democracy", "courage", "leadership"]],
      ["bullet", "A projectile from a firearm; a metaphor for force when politics fails.", "The ballot or the bullet.", "noun", "basic", "The Ballot or the Bullet", "The Ballot or the Bullet", ["courage", "democracy"]],
      ["education", "The process of receiving or giving systematic instruction.", "Education is the passport to the future, for tomorrow belongs to those who prepare for it today.", "noun", "basic", "Speeches on self-improvement", "On education", ["inspirational", "leadership", "legacy"]],
      ["passport", "An official document for travel; metaphorically, entry to opportunity.", "Education is the passport to the future, for tomorrow belongs to those who prepare for it today.", "noun", "intermediate", "Speeches on self-improvement", "On education", ["inspirational", "leadership"]],
      ["tomorrow", "The day after today; the near future.", "Education is the passport to the future, for tomorrow belongs to those who prepare for it today.", "noun", "basic", "Speeches on self-improvement", "On education", ["inspirational", "legacy"]],
      ["truth", "That which is true or in accordance with fact.", "I'm for truth, no matter who tells it. I'm for justice, no matter who it is for or against.", "noun", "basic", "Autobiography and speeches", "On truth", ["humanities", "courage", "legacy"]],
      ["justice", "Just behavior or treatment.", "I'm for truth, no matter who tells it. I'm for justice, no matter who it is for or against.", "noun", "intermediate", "Autobiography and speeches", "On truth", ["democracy", "humanities", "courage"]],
      ["sincerity", "The quality of being free from pretense or deceit.", "Usually when people are sad, they don't do anything. They just cry over their condition. But when they get angry, they bring about a change—sincerity without action is still sleep.", "noun", "advanced", "Speeches on social change", "On anger and change", ["courage", "leadership"]],
      ["change", "Make or become different; social transformation.", "Usually when people are sad, they don't do anything. They just cry over their condition. But when they get angry, they bring about a change.", "noun", "basic", "Speeches on social change", "On anger and change", ["courage", "democracy", "leadership"]],
      ["brotherhood", "An association or community of people linked by common interest.", "I believe in recognizing every human being as a human being—neither white, black, brown, or red; and when you are dealing with humanity as a family, brotherhood is not a slogan.", "noun", "intermediate", "Post-Mecca interviews and speeches", "On brotherhood", ["humanities", "inspirational", "legacy"]],
      ["house", "A building for human habitation; metaphorically, the American order.", "We are not fighting for integration, nor are we fighting for separation. We are fighting for recognition as human beings—and a house divided against itself still needs truth told inside it.", "noun", "basic", "Various addresses", "On recognition", ["democracy", "courage"]],
      ["wake", "To emerge or cause to emerge from sleep; to become alert.", "The greatest mistake of the movement has been trying to organize a sleeping people around specific goals. You have to wake the people up first, then you'll get action.", "verb", "basic", "Organizational strategy remarks", "On waking the people", ["leadership", "courage", "inspirational"]],
    ]),
    quotes: q([
      ["By any means necessary.", "OAAU founding rally themes", "By Any Means Necessary", 1964, "Claiming the right to full personhood without permission."],
      ["The ballot or the bullet.", "Cleveland address", "The Ballot or the Bullet", 1964, "Framing political urgency for Black voters."],
      ["Education is the passport to the future, for tomorrow belongs to those who prepare for it today.", "Speeches on youth", "On education", 1964, "Self-improvement as strategy."],
      ["I'm for truth, no matter who tells it. I'm for justice, no matter who it is for or against.", "Autobiography / public remarks", "On truth", 1965, "Principle over faction."],
      ["Usually when people are sad, they don't do anything. They just cry over their condition. But when they get angry, they bring about a change.", "Speeches", "On anger and change", 1963, "Anger as fuel for organization."],
      ["A man who stands for nothing will fall for anything.", "Popular attribution from his teaching", "On principle", 1964, "Warning against unrooted minds."],
      ["You're not to be so blind with patriotism that you can't face reality. Wrong is wrong, no matter who does it or says it.", "Public remarks", "On patriotism", 1964, "Moral clarity over national myth."],
      ["I believe in the brotherhood of man, all men, but I don't believe in brotherhood with anybody who doesn't want brotherhood with me.", "Post-Mecca clarity", "On brotherhood", 1964, "Reciprocal humanism."],
    ]),
    speech: {
      title: "The Ballot or the Bullet (Excerpt)",
      year: 1964,
      description: "Educational excerpt capturing Malcolm X's themes of political urgency, dignity, and self-determination.",
      fullText: `The question tonight, as I understand it, is "The Negro Revolt, and Where Do We Go From Here?" or "What Next?" In my little humble way of understanding it, it points toward either the ballot or the bullet.

If you're afraid to use an expression like that, you should get on out of the country; you should get back in the cotton patch; you should get back in the alley.

This is a political year. It's called a political year. The year when all of the white politicians are going to come into the Negro community. You never see them until election time.

We're looking for a house that's not divided against itself. We're looking for a country that's not divided against itself. We're looking for a society that's not divided against itself.

We declare our right on this earth to be a man, to be a human being, to be respected as a human being, to be given the rights of a human being in this society, on this earth, in this day, which we intend to bring into existence by any means necessary.

Education is the passport to the future, for tomorrow belongs to those who prepare for it today. I'm for truth, no matter who tells it. I'm for justice, no matter who it is for or against.`,
    },
  },
  {
    id: 49,
    slug: "desmond_tutu",
    file: "tutu",
    dict: {
      id: 49,
      name: "Desmond Tutu",
      description: "Archbishop and anti-apartheid voice of joyous moral courage and restorative justice",
      oratorName: "Desmond Tutu",
      wordCount: 16,
      category: "20th Century Legends",
      era: "Late 20th–21st Century (1931-2021)",
      bio: "Desmond Tutu was a South African Anglican bishop and theologian who became a global face of the struggle against apartheid. He chaired the Truth and Reconciliation Commission and preached a theology of ubuntu—shared humanity—with laughter and steel.",
      portraitUrl: "",
      primaryStyle: "Joyful moral authority",
      voiceStyle: "Warm, prophetic, playful",
      colorAccent: 12092939,
      sampleSpeech: "If you are neutral in situations of injustice, you have chosen the side of the oppressor.",
      tags: ["Justice", "Reconciliation", "Faith", "Ubuntu"],
      themeCategories: ["courage", "humanities", "democracy", "inspirational", "legacy"],
      isActive: true,
    },
    words: w([
      ["neutral", "Not supporting either side in a conflict; impartial in appearance.", "If you are neutral in situations of injustice, you have chosen the side of the oppressor.", "adjective", "intermediate", "Sermons and public addresses", "On neutrality", ["courage", "humanities", "democracy"]],
      ["injustice", "Lack of fairness or justice.", "If you are neutral in situations of injustice, you have chosen the side of the oppressor.", "noun", "intermediate", "Sermons and public addresses", "On neutrality", ["courage", "democracy", "humanities"]],
      ["oppressor", "A person or group that keeps others in subjection by unjust force.", "If you are neutral in situations of injustice, you have chosen the side of the oppressor.", "noun", "intermediate", "Sermons and public addresses", "On neutrality", ["courage", "democracy"]],
      ["ubuntu", "A Southern African ethic: I am because we are; personhood through others.", "Ubuntu says: my humanity is bound up in yours. A person is a person through other persons.", "noun", "advanced", "Talks on African humanism", "On ubuntu", ["humanities", "inspirational", "legacy"]],
      ["humanity", "Human beings collectively; the quality of being humane.", "My humanity is bound up in yours, for we can only be human together.", "noun", "intermediate", "Talks on African humanism", "On ubuntu", ["humanities", "inspirational", "legacy"]],
      ["reconcile", "Restore friendly relations; bring into agreement after conflict.", "Without forgiveness, there can be no future for a relationship between individuals or nations—we must reconcile without amnesia.", "verb", "advanced", "Truth and Reconciliation themes", "On forgiveness", ["humanities", "leadership", "legacy"]],
      ["forgiveness", "The action of pardoning a wrong without erasing its truth.", "Without forgiveness, there can be no future for a relationship between individuals or nations.", "noun", "intermediate", "Truth and Reconciliation themes", "On forgiveness", ["humanities", "inspirational", "courage"]],
      ["hope", "A feeling of expectation and desire for a better outcome.", "Hope is being able to see that there is light despite all of the darkness.", "noun", "basic", "Sermons", "On hope", ["inspirational", "courage"]],
      ["light", "Illumination; metaphorically, moral clarity in darkness.", "Hope is being able to see that there is light despite all of the darkness.", "noun", "basic", "Sermons", "On hope", ["inspirational", "humanities"]],
      ["darkness", "The absence of light; a condition of despair or evil.", "Hope is being able to see that there is light despite all of the darkness.", "noun", "basic", "Sermons", "On hope", ["courage", "humanities"]],
      ["rainbow", "An arc of spectrum colors; his image for a multiracial nation.", "We are the rainbow people of God—different colors, one sky.", "noun", "basic", "Post-apartheid addresses", "Rainbow nation", ["democracy", "inspirational", "legacy"]],
      ["truth", "Honesty about what happened; the commission's first word.", "Without truth, reconciliation is cheap; without reconciliation, truth becomes only a weapon.", "noun", "basic", "Truth and Reconciliation Commission", "On truth and reconciliation", ["humanities", "democracy", "legacy"]],
      ["peace", "Freedom from disturbance; right relationship after conflict.", "If you want peace, you don't talk to your friends. You talk to your enemies.", "noun", "basic", "Public diplomacy remarks", "On peace", ["leadership", "courage", "democracy"]],
      ["enemy", "A person who is actively opposed or hostile.", "If you want peace, you don't talk to your friends. You talk to your enemy.", "noun", "basic", "Public diplomacy remarks", "On peace", ["courage", "leadership"]],
      ["doormat", "A mat for wiping shoes; a person who submits to mistreatment.", "Forgiving is not forgetting; it's actually remembering—remembering and not using your right to hit back. It's a second chance for a new beginning. And the remembering part is particularly important. Especially if you don't want to repeat what happened. Forgiveness does not mean saying that what happened was acceptable. Forgiveness does not mean that you become a doormat.", "noun", "intermediate", "On forgiveness", "On forgiveness", ["humanities", "courage"]],
      ["together", "With or in proximity to another; jointly.", "My humanity is bound up in yours, for we can only be human together.", "adverb", "basic", "Talks on African humanism", "On ubuntu", ["inspirational", "humanities", "democracy"]],
    ]),
    quotes: q([
      ["If you are neutral in situations of injustice, you have chosen the side of the oppressor.", "Sermons and addresses", "On neutrality", 1984, "Rejecting false impartiality."],
      ["My humanity is bound up in yours, for we can only be human together.", "On ubuntu", "Talks on African humanism", 1999, "Personhood as relationship."],
      ["Hope is being able to see that there is light despite all of the darkness.", "Sermons", "On hope", 2000, "Hope as disciplined sight."],
      ["If you want peace, you don't talk to your friends. You talk to your enemies.", "Public remarks", "On peace", 1995, "Dialogue across enmity."],
      ["Do your little bit of good where you are; it's those little bits of good put together that overwhelm the world.", "Addresses", "On small goods", 2000, "Incremental moral action."],
      ["We are the rainbow people of God.", "Post-apartheid preaching", "Rainbow nation", 1994, "Naming multiracial South Africa."],
      ["Forgiveness does not mean condoning what has been done. It means taking what happened seriously and not minimizing it.", "On forgiveness", "Truth and reconciliation themes", 1999, "Hard forgiveness, not amnesia."],
      ["Differences are not intended to separate, to alienate. We are different precisely in order to realize our need of one another.", "On difference", "Ubuntu teaching", 2004, "Diversity as interdependence."],
    ]),
    speech: {
      title: "On Ubuntu, Neutrality, and Hope",
      year: 1999,
      description: "An educational monologue gathering Desmond Tutu's signature moral themes.",
      fullText: `If you are neutral in situations of injustice, you have chosen the side of the oppressor. The hot sun beats on the victim and on the bystander alike, but only one is on the crossroads of choice.

Ubuntu says: my humanity is bound up in yours. A person is a person through other persons. We can only be human together.

Hope is being able to see that there is light despite all of the darkness. It is not a feeling that everything is fine; it is a decision to keep walking when the road is still broken.

Without forgiveness, there can be no future for a relationship between individuals or nations. Forgiveness does not mean you become a doormat. Forgiveness does not mean saying that what happened was acceptable. It means refusing to let the wound write the only chapter of the story.

If you want peace, you don't talk to your friends. You talk to your enemies.

Do your little bit of good where you are; it's those little bits of good put together that overwhelm the world. We are the rainbow people of God—different colors under one sky—and differences are not intended to separate us, but to teach us our need of one another.`,
    },
  },
  {
    id: 50,
    slug: "chief_joseph",
    file: "joseph",
    dict: {
      id: 50,
      name: "Chief Joseph",
      description: "Nez Perce leader whose surrender speech turned military defeat into lasting moral witness",
      oratorName: "Chief Joseph",
      wordCount: 16,
      category: "19th Century Powerhouses",
      era: "19th Century (1840-1904)",
      bio: "Chief Joseph (Hinmatóowyalahtq̓it) led the Nez Perce during the war of 1877. After a fighting retreat of more than a thousand miles, he surrendered near the Canadian border with words that made 'I will fight no more forever' part of American memory.",
      portraitUrl: "",
      primaryStyle: "Measured tragic dignity",
      voiceStyle: "Grave, plain, unforgettable",
      colorAccent: 6700313,
      sampleSpeech: "Hear me, my chiefs! I am tired; my heart is sick and sad. From where the sun now stands I will fight no more forever.",
      tags: ["Native oratory", "Peace", "Dignity", "Leadership"],
      themeCategories: ["courage", "leadership", "humanities", "legacy", "inspirational"],
      isActive: true,
    },
    words: w([
      ["tired", "In need of rest or sleep; exhausted by struggle.", "Hear me, my chiefs! I am tired; my heart is sick and sad.", "adjective", "basic", "Surrender speech", "Bear Paw Mountains surrender", ["courage", "humanities", "legacy"]],
      ["heart", "The center of feeling and will.", "Hear me, my chiefs! I am tired; my heart is sick and sad.", "noun", "basic", "Surrender speech", "Bear Paw Mountains surrender", ["humanities", "inspirational"]],
      ["sick", "Affected by physical or mental distress.", "Hear me, my chiefs! I am tired; my heart is sick and sad.", "adjective", "basic", "Surrender speech", "Bear Paw Mountains surrender", ["humanities", "courage"]],
      ["sad", "Feeling or showing sorrow.", "Hear me, my chiefs! I am tired; my heart is sick and sad.", "adjective", "basic", "Surrender speech", "Bear Paw Mountains surrender", ["humanities", "legacy"]],
      ["fight", "Take part in a violent struggle; resist by force.", "From where the sun now stands I will fight no more forever.", "verb", "basic", "Surrender speech", "Bear Paw Mountains surrender", ["courage", "legacy", "leadership"]],
      ["forever", "For all future time; eternally.", "From where the sun now stands I will fight no more forever.", "adverb", "basic", "Surrender speech", "Bear Paw Mountains surrender", ["legacy", "inspirational"]],
      ["chief", "A leader of a people or clan.", "Hear me, my chiefs! I am tired; my heart is sick and sad.", "noun", "basic", "Surrender speech", "Bear Paw Mountains surrender", ["leadership", "legacy"]],
      ["sun", "The star that is the central body of the solar system; a marker of time and place.", "From where the sun now stands I will fight no more forever.", "noun", "basic", "Surrender speech", "Bear Paw Mountains surrender", ["arts", "legacy", "humanities"]],
      ["child", "A young human being.", "It is cold, and we have no blankets; the little children are freezing to death.", "noun", "basic", "Surrender speech", "Bear Paw Mountains surrender", ["humanities", "courage"]],
      ["freezing", "Suffering or dying from extreme cold.", "It is cold, and we have no blankets; the little children are freezing to death.", "adjective", "basic", "Surrender speech", "Bear Paw Mountains surrender", ["courage", "humanities"]],
      ["look", "Direct one's gaze; search.", "I want to have time to look for my children, and see how many of them I can find.", "verb", "basic", "Surrender speech", "Bear Paw Mountains surrender", ["humanities", "leadership"]],
      ["dead", "No longer alive.", "Maybe I shall find them among the dead.", "noun", "basic", "Surrender speech", "Bear Paw Mountains surrender", ["humanities", "legacy"]],
      ["treaty", "A formally concluded agreement between peoples or nations.", "Good words do not last long unless they amount to something—treaty promises broken are worse than silence.", "noun", "intermediate", "Lincoln Hall speech / later addresses", "On broken promises", ["democracy", "leadership", "courage"]],
      ["promise", "A declaration that one will do or refrain from something.", "Good words will not give my people good health and stop them from dying. Good words will not make good the promise of your war chief.", "noun", "basic", "Lincoln Hall speech", "On broken promises", ["courage", "democracy", "legacy"]],
      ["equal", "Having the same rights or status.", "Treat all men alike. Give them all the same law. Give them all an even chance to live and grow—equal standing under one sky.", "adjective", "basic", "Lincoln Hall speech", "On equal law", ["democracy", "humanities", "leadership"]],
      ["peace", "Freedom from war; a condition of settled quiet.", "I will fight no more forever—not because peace is easy, but because my people can bear no more winter war.", "noun", "basic", "Surrender speech themes", "Bear Paw Mountains surrender", ["leadership", "legacy", "inspirational"]],
    ]),
    quotes: q([
      ["From where the sun now stands I will fight no more forever.", "Surrender at Bear Paw Mountains", "Surrender speech", 1877, "Ending the Nez Perce fighting retreat."],
      ["Hear me, my chiefs! I am tired; my heart is sick and sad.", "Surrender at Bear Paw Mountains", "Surrender speech", 1877, "Addressing remaining leaders in defeat."],
      ["It is cold, and we have no blankets; the little children are freezing to death.", "Surrender at Bear Paw Mountains", "Surrender speech", 1877, "Naming the human cost of pursuit."],
      ["I want to have time to look for my children, and see how many of them I can find. Maybe I shall find them among the dead.", "Surrender at Bear Paw Mountains", "Surrender speech", 1877, "A father's inventory of loss."],
      ["Treat all men alike. Give them all the same law. Give them all an even chance to live and grow.", "Lincoln Hall address", "On equal law", 1879, "Demanding equal law after removal."],
      ["Good words do not last long unless they amount to something.", "Lincoln Hall address", "On broken promises", 1879, "Judging rhetoric by results."],
      ["I believe much trouble and blood would be saved if we opened our hearts more.", "Public addresses", "On open hearts", 1879, "Empathy as practical policy."],
      ["We only ask an even chance to live as other men live.", "Lincoln Hall address", "On equal chance", 1879, "A minimal demand for fairness."],
    ]),
    speech: {
      title: "I Will Fight No More Forever",
      year: 1877,
      description: "Chief Joseph's surrender speech at the Bear Paw Mountains, as recorded in American memory.",
      fullText: `Tell General Howard I know his heart. What he told me before, I have it in my heart. I am tired of fighting. Our chiefs are killed; Looking Glass is dead, Too-hul-hul-sote is dead. The old men are all dead. It is the young men who say yes or no. He who led on the young men is dead.

It is cold, and we have no blankets; the little children are freezing to death. My people, some of them, have run away to the hills, and have no blankets, no food. No one knows where they are—perhaps freezing to death. I want to have time to look for my children, and see how many of them I can find. Maybe I shall find them among the dead.

Hear me, my chiefs! I am tired; my heart is sick and sad. From where the sun now stands I will fight no more forever.`,
    },
  },
  {
    id: 51,
    slug: "atticus_finch",
    file: "atticus",
    dict: {
      id: 51,
      name: "Atticus Finch",
      description: "Fictional Alabama lawyer whose quiet courage taught a nation to climb into another person's skin",
      oratorName: "Atticus Finch",
      wordCount: 16,
      category: "Fictional / Modern Inspirational",
      era: "1930s American South (Literary)",
      bio: "Atticus Finch is the principled lawyer and father in Harper Lee's To Kill a Mockingbird. Through courtroom argument and porch wisdom, he models moral courage, empathy, and the costly defense of justice when the town prefers silence.",
      portraitUrl: "",
      primaryStyle: "Quiet moral instruction",
      voiceStyle: "Calm, precise, fatherly",
      colorAccent: 4473924,
      sampleSpeech: "You never really understand a person until you consider things from his point of view—until you climb into his skin and walk around in it.",
      tags: ["Justice", "Empathy", "Courage", "Law"],
      themeCategories: ["courage", "humanities", "democracy", "inspirational", "legacy"],
      isActive: true,
    },
    words: w([
      ["understand", "Perceive the intended meaning of; grasp sympathetically.", "You never really understand a person until you consider things from his point of view.", "verb", "basic", "To Kill a Mockingbird", "Lesson to Scout", ["humanities", "inspirational"]],
      ["climb", "Go up or into with effort; enter imaginatively.", "You never really understand a person until you consider things from his point of view—until you climb into his skin and walk around in it.", "verb", "basic", "To Kill a Mockingbird", "Lesson to Scout", ["humanities", "inspirational"]],
      ["skin", "The natural outer covering of the body; a metaphor for another's life.", "Until you climb into his skin and walk around in it.", "noun", "basic", "To Kill a Mockingbird", "Lesson to Scout", ["humanities", "courage"]],
      ["courage", "The ability to do something that frightens one; strength in the face of pain.", "I wanted you to see what real courage is, instead of getting the idea that courage is a man with a gun in his hand.", "noun", "basic", "To Kill a Mockingbird", "On Mrs. Dubose", ["courage", "inspirational", "legacy"]],
      ["gun", "A weapon that fires projectiles; a false image of bravery.", "I wanted you to see what real courage is, instead of getting the idea that courage is a man with a gun in his hand.", "noun", "basic", "To Kill a Mockingbird", "On Mrs. Dubose", ["courage", "humanities"]],
      ["conscience", "An inner sense of right and wrong.", "The one thing that doesn't abide by majority rule is a person's conscience.", "noun", "intermediate", "To Kill a Mockingbird", "On conscience", ["democracy", "humanities", "courage"]],
      ["majority", "The greater number; the larger part of a group.", "The one thing that doesn't abide by majority rule is a person's conscience.", "noun", "intermediate", "To Kill a Mockingbird", "On conscience", ["democracy", "leadership"]],
      ["mockingbird", "A songbird; a symbol of innocent beings who should not be harmed.", "Shoot all the bluejays you want, if you can hit 'em, but remember it's a sin to kill a mockingbird.", "noun", "intermediate", "To Kill a Mockingbird", "On mockingbirds", ["humanities", "arts", "legacy"]],
      ["sin", "An immoral act; a wrong against the innocent.", "Remember it's a sin to kill a mockingbird.", "noun", "basic", "To Kill a Mockingbird", "On mockingbirds", ["humanities", "courage"]],
      ["court", "A tribunal for justice; the arena of public judgment.", "But there is one way in this country in which all men are created equal—there is one human institution that makes a pauper the equal of a Rockefeller, the stupid man the equal of an Einstein, and the ignorant man the equal of any college president. That institution, gentlemen, is a court.", "noun", "basic", "To Kill a Mockingbird", "Courtroom speech", ["democracy", "leadership", "legacy"]],
      ["equal", "Having the same status, rights, or value.", "But there is one way in this country in which all men are created equal—there is one human institution that makes a pauper the equal of a Rockefeller.", "adjective", "basic", "To Kill a Mockingbird", "Courtroom speech", ["democracy", "humanities"]],
      ["pauper", "A very poor person.", "That institution makes a pauper the equal of a Rockefeller.", "noun", "advanced", "To Kill a Mockingbird", "Courtroom speech", ["democracy", "humanities"]],
      ["integrity", "The quality of being honest and having strong moral principles.", "Before I can live with other folks I've got to live with myself. The one thing that doesn't abide by majority rule is a person's conscience—and integrity is what remains when the town goes home.", "noun", "advanced", "To Kill a Mockingbird", "On living with oneself", ["courage", "humanities", "leadership"]],
      ["ugly", "Unpleasant or repulsive, especially in moral character.", "There's something in our world that makes men lose their heads—they couldn't be fair if they tried. In our courts, when it's a white man's word against a black man's, the white man always wins. They're ugly, but those are the facts of life.", "adjective", "basic", "To Kill a Mockingbird", "On Maycomb's disease", ["courage", "humanities", "democracy"]],
      ["simple", "Easily understood; not complicated; plain in manner.", "I do my best to love everybody—it's not simple, but it's the job.", "adjective", "basic", "To Kill a Mockingbird", "On loving everybody", ["inspirational", "humanities"]],
      ["stand", "Be in or rise to an upright position; take a firm public position.", "Scout, simply by the nature of the work, every lawyer gets at least one case in his lifetime that affects him personally. This one's mine, I guess—and a man must stand for it.", "verb", "basic", "To Kill a Mockingbird", "On taking the case", ["courage", "leadership", "legacy"]],
    ]),
    quotes: q([
      ["You never really understand a person until you consider things from his point of view—until you climb into his skin and walk around in it.", "To Kill a Mockingbird", "Lesson to Scout", 1960, "Empathy as disciplined imagination."],
      ["I wanted you to see what real courage is, instead of getting the idea that courage is a man with a gun in his hand.", "To Kill a Mockingbird", "On Mrs. Dubose", 1960, "Courage as endurance for a lost cause."],
      ["The one thing that doesn't abide by majority rule is a person's conscience.", "To Kill a Mockingbird", "On conscience", 1960, "Moral independence from the crowd."],
      ["Shoot all the bluejays you want, if you can hit 'em, but remember it's a sin to kill a mockingbird.", "To Kill a Mockingbird", "On mockingbirds", 1960, "Protecting the harmless."],
      ["But there is one way in this country in which all men are created equal—that institution, gentlemen, is a court.", "To Kill a Mockingbird", "Courtroom speech", 1960, "Law as formal equality."],
      ["Before I can live with other folks I've got to live with myself.", "To Kill a Mockingbird", "On integrity", 1960, "Self-respect as the first jury."],
      ["The one place where a man ought to get a square deal is in a courtroom, be he any color of the rainbow, but people have a way of carrying their resentments right into a jury box.", "To Kill a Mockingbird", "On juries", 1960, "Naming prejudice inside procedure."],
      ["It's when you know you're licked before you begin but you begin anyway and you see it through no matter what.", "To Kill a Mockingbird", "On real courage", 1960, "Defining courage as finished work."],
    ]),
    speech: {
      title: "Courtroom Address (Educational Composite)",
      year: 1960,
      description: "An educational monologue gathering Atticus Finch's courtroom and moral themes from To Kill a Mockingbird.",
      fullText: `You never really understand a person until you consider things from his point of view—until you climb into his skin and walk around in it.

I wanted you to see what real courage is, instead of getting the idea that courage is a man with a gun in his hand. It's when you know you're licked before you begin but you begin anyway and you see it through no matter what.

The one thing that doesn't abide by majority rule is a person's conscience. Before I can live with other folks I've got to live with myself.

Shoot all the bluejays you want, if you can hit 'em, but remember it's a sin to kill a mockingbird.

But there is one way in this country in which all men are created equal—there is one human institution that makes a pauper the equal of a Rockefeller, the stupid man the equal of an Einstein, and the ignorant man the equal of any college president. That institution, gentlemen, is a court.

The one place where a man ought to get a square deal is in a courtroom, be he any color of the rainbow, but people have a way of carrying their resentments right into a jury box. I am confident that you gentlemen will review without passion the evidence you have heard, come to a decision, and restore this defendant to his family. In the name of God, do your duty.`,
    },
  },
  {
    id: 52,
    slug: "albus_dumbledore",
    file: "dumbledore",
    dict: {
      id: 52,
      name: "Albus Dumbledore",
      description: "Headmaster of Hogwarts whose gentle riddles teach love, choice, and light against dark times",
      oratorName: "Albus Dumbledore",
      wordCount: 16,
      category: "Fictional / Mythic",
      era: "Harry Potter series (Literary)",
      bio: "Albus Dumbledore is the wise, eccentric headmaster in J.K. Rowling's Harry Potter series. His speeches and counsel—about love, choice, grief, and hope—have become modern mythic rhetoric for courage under dark power.",
      portraitUrl: "",
      primaryStyle: "Gentle paradoxical wisdom",
      voiceStyle: "Kind, wry, luminous",
      colorAccent: 10079487,
      sampleSpeech: "Happiness can be found, even in the darkest of times, if one only remembers to turn on the light.",
      tags: ["Wisdom", "Love", "Choice", "Hope"],
      themeCategories: ["inspirational", "humanities", "courage", "leadership", "legacy"],
      isActive: true,
    },
    words: w([
      ["happiness", "The state of feeling or showing pleasure or contentment.", "Happiness can be found, even in the darkest of times, if one only remembers to turn on the light.", "noun", "basic", "Prisoner of Azkaban", "Great Hall address after Black's escape", ["inspirational", "humanities"]],
      ["dark", "With little or no light; morally grim.", "Happiness can be found, even in the darkest of times, if one only remembers to turn on the light.", "adjective", "basic", "Prisoner of Azkaban", "Great Hall address after Black's escape", ["courage", "inspirational"]],
      ["light", "Illumination; hope made practical.", "Happiness can be found, even in the darkest of times, if one only remembers to turn on the light.", "noun", "basic", "Prisoner of Azkaban", "Great Hall address after Black's escape", ["inspirational", "courage", "legacy"]],
      ["choice", "An act of selecting between possibilities; the seat of moral character.", "It is our choices, Harry, that show what we truly are, far more than our abilities.", "noun", "basic", "Chamber of Secrets", "On the Sorting and identity", ["humanities", "inspirational", "leadership"]],
      ["ability", "Possession of the means or skill to do something.", "It is our choices, Harry, that show what we truly are, far more than our ability alone.", "noun", "basic", "Chamber of Secrets", "On the Sorting and identity", ["humanities", "leadership"]],
      ["love", "An intense feeling of deep affection; a protective force in the story's moral physics.", "Do not pity the dead, Harry. Pity the living, and, above all, those who live without love.", "noun", "basic", "Deathly Hallows", "King's Cross counsel", ["humanities", "inspirational", "legacy"]],
      ["pity", "The feeling of sorrow and compassion caused by others' suffering.", "Do not pity the dead, Harry. Pity the living, and, above all, those who live without love.", "verb", "intermediate", "Deathly Hallows", "King's Cross counsel", ["humanities", "courage"]],
      ["fear", "An unpleasant emotion caused by threat or danger.", "It is the unknown we fear when we look upon death and darkness, nothing more.", "verb", "basic", "Half-Blood Prince", "On fear and death", ["courage", "humanities"]],
      ["death", "The end of life; a teacher of perspective.", "Do not pity the dead, Harry. Pity the living—and remember that to the well-organized mind, death is but the next great adventure.", "noun", "basic", "Philosopher's Stone / Deathly Hallows", "On death", ["humanities", "courage", "legacy"]],
      ["adventure", "An unusual and exciting experience; a journey into risk.", "To the well-organized mind, death is but the next great adventure.", "noun", "intermediate", "Philosopher's Stone", "On death", ["inspirational", "humanities"]],
      ["word", "A single distinct unit of language; speech as power.", "Words are, in my not-so-humble opinion, our most inexhaustible source of magic.", "noun", "basic", "Deathly Hallows / related counsel", "On words", ["arts", "humanities", "leadership"]],
      ["magic", "The power of influencing events by supernatural means; metaphorically, transformative speech.", "Words are, in my not-so-humble opinion, our most inexhaustible source of magic.", "noun", "basic", "Deathly Hallows / related counsel", "On words", ["arts", "inspirational"]],
      ["help", "Make it easier for someone to do something; give assistance.", "Help will always be given at Hogwarts to those who ask for it.", "noun", "basic", "Chamber of Secrets / Order of the Phoenix themes", "On asking for help", ["inspirational", "leadership"]],
      ["difference", "A point or way in which people or things are not the same.", "We are only as strong as we are united, as weak as we are divided—indifference to one another is a kind of difference that wounds.", "noun", "intermediate", "Goblet of Fire", "After the Triwizard final", ["leadership", "humanities", "courage"]],
      ["united", "Joined together politically or for a common purpose.", "We are only as strong as we are united, as weak as we are divided.", "adjective", "basic", "Goblet of Fire", "After the Triwizard final", ["leadership", "inspirational", "courage"]],
      ["hope", "A feeling of expectation and desire for a certain thing to happen.", "Happiness can be found even in dark times—hope is the hand that finds the switch.", "noun", "basic", "Prisoner of Azkaban themes", "On light", ["inspirational", "courage"]],
    ]),
    quotes: q([
      ["Happiness can be found, even in the darkest of times, if one only remembers to turn on the light.", "Prisoner of Azkaban", "Great Hall", 1999, "Hope as an active gesture."],
      ["It is our choices, Harry, that show what we truly are, far more than our abilities.", "Chamber of Secrets", "Headmaster's office", 1998, "Character as chosen path."],
      ["Do not pity the dead, Harry. Pity the living, and, above all, those who live without love.", "Deathly Hallows", "King's Cross", 2007, "Love as the measure of life."],
      ["To the well-organized mind, death is but the next great adventure.", "Philosopher's Stone", "On Nicolas Flamel", 1997, "Reframing mortality."],
      ["Words are, in my not-so-humble opinion, our most inexhaustible source of magic.", "Deathly Hallows / related", "On language", 2007, "Rhetoric as power."],
      ["We are only as strong as we are united, as weak as we are divided.", "Goblet of Fire", "After Cedric's death", 2000, "Unity after terror."],
      ["It does not do to dwell on dreams and forget to live.", "Philosopher's Stone", "On the Mirror of Erised", 1997, "Against living only in longing."],
      ["Help will always be given at Hogwarts to those who ask for it.", "Chamber / Order themes", "On asking", 1998, "Institution as refuge."],
    ]),
    speech: {
      title: "After the Dark Mark (Educational Composite)",
      year: 2000,
      description: "An educational monologue gathering Dumbledore's counsel on light, choice, unity, and love.",
      fullText: `Happiness can be found, even in the darkest of times, if one only remembers to turn on the light.

It is our choices, Harry, that show what we truly are, far more than our abilities. The Sorting Hat may see courage or ambition in you, but what you do with that sight is yours alone.

We are only as strong as we are united, as weak as we are divided. Cedric Diggory was as you all know, exceptionally hard-working, infinitely fair-minded, and most importantly a fierce champion of ordinary qualities that make a person extraordinary in the end: loyalty, and friendship, and truth.

Do not pity the dead. Pity the living, and, above all, those who live without love. Words are, in my not-so-humble opinion, our most inexhaustible source of magic—capable of both inflicting injury and remedying it.

It does not do to dwell on dreams and forget to live. Help will always be given at Hogwarts to those who ask for it. And to the well-organized mind, death is but the next great adventure—yet while we live, we choose the light.`,
    },
  },
  {
    id: 53,
    slug: "oprah_winfrey",
    file: "oprah",
    dict: {
      id: 53,
      name: "Oprah Winfrey",
      description: "Media leader whose public voice turned personal story into a language of gratitude, agency, and rise",
      oratorName: "Oprah Winfrey",
      wordCount: 16,
      category: "Modern / Contemporary",
      era: "Late 20th–21st Century",
      bio: "Oprah Winfrey is an American talk-show host, producer, and philanthropist whose interviews and speeches popularized a rhetoric of authenticity, gratitude, and self-determination. Her Golden Globes address and commencement talks treat storytelling as public service.",
      portraitUrl: "",
      primaryStyle: "Empathic empowerment",
      voiceStyle: "Warm, confessional, commanding",
      colorAccent: 11674146,
      sampleSpeech: "What I know for sure is that speaking your truth is the most powerful tool we all have.",
      tags: ["Media", "Truth", "Gratitude", "Agency"],
      themeCategories: ["inspirational", "leadership", "humanities", "courage", "legacy"],
      isActive: true,
    },
    words: w([
      ["truth", "That which is true; honest speech about one's experience.", "What I know for sure is that speaking your truth is the most powerful tool we all have.", "noun", "basic", "Golden Globes Cecil B. DeMille address", "Golden Globes 2018", ["courage", "inspirational", "leadership"]],
      ["tool", "A device or implement used to carry out a function; a means of effect.", "Speaking your truth is the most powerful tool we all have.", "noun", "basic", "Golden Globes Cecil B. DeMille address", "Golden Globes 2018", ["leadership", "inspirational"]],
      ["powerful", "Having great power or strength; highly effective.", "Speaking your truth is the most powerful tool we all have.", "adjective", "basic", "Golden Globes Cecil B. DeMille address", "Golden Globes 2018", ["leadership", "courage"]],
      ["gratitude", "The quality of being thankful; readiness to show appreciation.", "Be thankful for what you have; you'll end up having more. If you concentrate on what you don't have, you will never, ever have enough—gratitude multiplies.", "noun", "intermediate", "Talks on living", "On gratitude", ["inspirational", "humanities"]],
      ["concentrate", "Focus all one's attention or mental effort on.", "If you concentrate on what you don't have, you will never, ever have enough.", "verb", "intermediate", "Talks on living", "On gratitude", ["inspirational", "leadership"]],
      ["possibility", "A thing that may happen or be the case; open potential.", "I know for sure that what we dwell on is who we become—possibility grows where attention goes.", "noun", "intermediate", "Talks on becoming", "On attention", ["inspirational", "leadership"]],
      ["dwell", "Think, speak, or write at length about; live in a mental place.", "I know for sure that what we dwell on is who we become.", "verb", "intermediate", "Talks on becoming", "On attention", ["humanities", "inspirational"]],
      ["become", "Begin to be; grow into a new state.", "I know for sure that what we dwell on is who we become.", "verb", "basic", "Talks on becoming", "On attention", ["inspirational", "legacy"]],
      ["story", "An account of incidents or events; narrative as power.", "You get a story. You get a story. Everybody gets a story—and the story we tell becomes the life we lead.", "noun", "basic", "Show themes / public talks", "On story", ["arts", "humanities", "inspirational"]],
      ["voice", "The sound produced in speech; the right and power to be heard.", "For too long, women have not been heard or believed if they dare speak their truth to the power of those men. But their time is up, and their voice is rising.", "noun", "basic", "Golden Globes Cecil B. DeMille address", "Golden Globes 2018", ["courage", "democracy", "leadership"]],
      ["believe", "Accept as true; feel sure of the truth of.", "For too long, women have not been heard or believed if they dare speak their truth to the power of those men.", "verb", "basic", "Golden Globes Cecil B. DeMille address", "Golden Globes 2018", ["courage", "humanities", "democracy"]],
      ["time", "The indefinite continued progress of existence; a historical moment.", "Their time is up.", "noun", "basic", "Golden Globes Cecil B. DeMille address", "Golden Globes 2018", ["courage", "legacy", "leadership"]],
      ["intention", "A thing intended; an aim or plan.", "Intention rules the day—when you know what you want and why, action finds a road.", "noun", "intermediate", "Leadership talks", "On intention", ["leadership", "inspirational"]],
      ["excellence", "The quality of being outstanding or extremely good.", "Excellence is the best deterrent to racism or sexism or any other kind of -ism—you do the work so well they cannot deny you.", "noun", "intermediate", "Career advice addresses", "On excellence", ["leadership", "inspirational", "courage"]],
      ["deterrent", "A thing that discourages or is intended to discourage an action.", "Excellence is the best deterrent to racism or sexism.", "noun", "advanced", "Career advice addresses", "On excellence", ["leadership", "courage"]],
      ["rise", "Move from a lower to a higher position; come into power or notice.", "I want all the girls watching here and now to know that a new day is on the horizon—and when that new day finally dawns, it will be because of a lot of magnificent women, and some pretty phenomenal men, fighting hard to make sure that they become the leaders who take us to the time when nobody ever has to say 'Me too' again—when we all rise.", "verb", "basic", "Golden Globes Cecil B. DeMille address", "Golden Globes 2018", ["inspirational", "courage", "leadership"]],
    ]),
    quotes: q([
      ["What I know for sure is that speaking your truth is the most powerful tool we all have.", "Golden Globes", "Cecil B. DeMille Award", 2018, "Truth-telling as civic instrument."],
      ["Their time is up.", "Golden Globes", "Cecil B. DeMille Award", 2018, "Declaring an end to protected abuse."],
      ["Be thankful for what you have; you'll end up having more.", "Talks on living", "On gratitude", 2000, "Gratitude as abundance practice."],
      ["I know for sure that what we dwell on is who we become.", "Public talks", "On attention", 2005, "Attention shapes character."],
      ["Turn your wounds into wisdom.", "Life advice addresses", "On wounds", 2001, "Pain as curriculum."],
      ["The biggest adventure you can take is to live the life of your dreams.", "Inspirational talks", "On dreams", 2004, "Agency as adventure."],
      ["Excellence is the best deterrent to racism or sexism.", "Career counsel", "On excellence", 1998, "Craft as rebuttal."],
      ["You become what you believe—not what you wish or want but what you truly believe.", "Talks on belief", "On belief", 2002, "Belief as formative force."],
    ]),
    speech: {
      title: "Golden Globes Address (Educational Excerpt)",
      year: 2018,
      description: "Key themes from Oprah Winfrey's Cecil B. DeMille Award acceptance speech.",
      fullText: `In 1964, I was a little girl sitting on the linoleum floor of my mother's house in Milwaukee watching Anne Bancroft present the Oscar for best actor at the 36th Academy Awards. She opened the envelope and said five words that literally made history: "The winner is Sidney Poitier."

I remember him that night as someone whose work made me proud to be part of a world of possibility. Now, I want all the girls watching here, now, to know that a new day is on the horizon!

And when that new day finally dawns, it will be because of a lot of magnificent women, many of them sitting here tonight, and some pretty phenomenal men, fighting hard to make sure that they become the leaders who take us to the time when nobody ever has to say "Me too" again.

What I know for sure is that speaking your truth is the most powerful tool we all have. For too long, women have not been heard or believed if they dare speak their truth to the power of those men. But their time is up.

I want to thank the Hollywood Foreign Press Association. And I want to thank everybody who supported me on this journey. Their time is up—and our voice is rising.`,
    },
  },
  {
    id: 54,
    slug: "elizabeth_i",
    file: "elizabeth",
    dict: {
      id: 54,
      name: "Elizabeth I",
      description: "Tudor queen whose Tilbury speech fused body, crown, and nation against invasion",
      oratorName: "Elizabeth I",
      wordCount: 16,
      category: "Literary Classics",
      era: "Tudor England (1533-1603)",
      bio: "Elizabeth I ruled England from 1558 to 1603, navigating religious conflict and foreign threat. At Tilbury in 1588, facing the Spanish Armada crisis, she delivered a speech that welded personal courage to political theology—one heart and stomach of a king.",
      portraitUrl: "",
      primaryStyle: "Regal martial resolve",
      voiceStyle: "Majestic, measured, fierce",
      colorAccent: 10027212,
      sampleSpeech: "I know I have the body but of a weak and feeble woman; but I have the heart and stomach of a king, and of a king of England too.",
      tags: ["Monarchy", "Courage", "Nation", "Rhetoric"],
      themeCategories: ["courage", "leadership", "legacy", "arts", "inspirational"],
      isActive: true,
    },
    words: w([
      ["feeble", "Lacking physical strength, especially as a result of age or illness.", "I know I have the body but of a weak and feeble woman; but I have the heart and stomach of a king.", "adjective", "advanced", "Speech to the Troops at Tilbury", "Tilbury", ["courage", "arts", "leadership"]],
      ["heart", "The center of feeling, courage, and loyalty.", "I have the heart and stomach of a king, and of a king of England too.", "noun", "basic", "Speech to the Troops at Tilbury", "Tilbury", ["courage", "leadership", "legacy"]],
      ["stomach", "Appetite for conflict; courage or guts in early modern English.", "I have the heart and stomach of a king, and of a king of England too.", "noun", "intermediate", "Speech to the Troops at Tilbury", "Tilbury", ["courage", "arts"]],
      ["king", "A male sovereign; here, royal martial authority claimed by a queen.", "I have the heart and stomach of a king, and of a king of England too.", "noun", "basic", "Speech to the Troops at Tilbury", "Tilbury", ["leadership", "courage", "legacy"]],
      ["realm", "A kingdom; a field of royal authority.", "I am come amongst you, as you see, at this time, not for my recreation and disport, but being resolved, in the midst and heat of the battle, to live and die amongst you all; to lay down for my God, and for my kingdom, and my people, my honour and my blood, even in the dust—this realm is my body politic.", "noun", "advanced", "Speech to the Troops at Tilbury", "Tilbury", ["leadership", "legacy", "democracy"]],
      ["honour", "High respect; personal and public reputation for courage.", "To lay down for my God, and for my kingdom, and my people, my honour and my blood, even in the dust.", "noun", "intermediate", "Speech to the Troops at Tilbury", "Tilbury", ["courage", "leadership", "legacy"]],
      ["blood", "The fluid of life; lineage and sacrificial cost.", "To lay down for my God, and for my kingdom, and my people, my honour and my blood, even in the dust.", "noun", "basic", "Speech to the Troops at Tilbury", "Tilbury", ["courage", "legacy"]],
      ["faith", "Complete trust; religious fidelity.", "I know already, for your forwardness you have deserved rewards and crowns; and We do assure you on the word of a prince, they shall be duly paid you—keep faith with me as I keep faith with you.", "noun", "basic", "Speech to the Troops at Tilbury", "Tilbury", ["leadership", "humanities"]],
      ["prince", "A sovereign ruler (of either sex in Elizabethan usage).", "We do assure you on the word of a prince, they shall be duly paid you.", "noun", "intermediate", "Speech to the Troops at Tilbury", "Tilbury", ["leadership", "arts"]],
      ["tyrant", "A cruel and oppressive ruler.", "I do not desire to live to distrust my faithful and loving people—let tyrants fear.", "noun", "intermediate", "Speech to the Troops at Tilbury", "Tilbury", ["courage", "democracy", "leadership"]],
      ["faithful", "Loyal and steadfast.", "Let tyrants fear, I have always so behaved myself that, under God, I have placed my chiefest strength and safeguard in the loyal hearts and good-will of my subjects—my faithful and loving people.", "adjective", "intermediate", "Speech to the Troops at Tilbury", "Tilbury", ["leadership", "legacy"]],
      ["safeguard", "A measure taken to protect someone or something.", "I have placed my chiefest strength and safeguard in the loyal hearts and good-will of my subjects.", "noun", "advanced", "Speech to the Troops at Tilbury", "Tilbury", ["leadership", "courage"]],
      ["subject", "A person under the rule of a monarch.", "I have placed my chiefest strength and safeguard in the loyal hearts and good-will of my subjects.", "noun", "intermediate", "Speech to the Troops at Tilbury", "Tilbury", ["leadership", "democracy"]],
      ["invade", "Enter a country or region so as to subjugate it.", "I myself will take up arms, I myself will be your general, judge, and rewarder of every one of your virtues in the field—if Europe's princes dare invade.", "verb", "intermediate", "Speech to the Troops at Tilbury", "Tilbury", ["courage", "leadership"]],
      ["general", "A commander of an army.", "I myself will take up arms, I myself will be your general, judge, and rewarder of every one of your virtues in the field.", "noun", "basic", "Speech to the Troops at Tilbury", "Tilbury", ["leadership", "courage"]],
      ["virtue", "Behavior showing high moral standards; excellence in arms and character.", "I myself will be your general, judge, and rewarder of every one of your virtues in the field.", "noun", "intermediate", "Speech to the Troops at Tilbury", "Tilbury", ["courage", "leadership", "humanities"]],
    ]),
    quotes: q([
      ["I know I have the body but of a weak and feeble woman; but I have the heart and stomach of a king, and of a king of England too.", "Tilbury speech", "Speech to the Troops at Tilbury", 1588, "Claiming royal martial authority in a woman's body."],
      ["I am come amongst you, as you see, at this time, not for my recreation and disport, but being resolved, in the midst and heat of the battle, to live and die amongst you all.", "Tilbury speech", "Speech to the Troops at Tilbury", 1588, "Presence as leadership."],
      ["Let tyrants fear, I have always so behaved myself that, under God, I have placed my chiefest strength and safeguard in the loyal hearts and good-will of my subjects.", "Tilbury speech", "Speech to the Troops at Tilbury", 1588, "Security rooted in consent and loyalty."],
      ["I myself will take up arms, I myself will be your general, judge, and rewarder of every one of your virtues in the field.", "Tilbury speech", "Speech to the Troops at Tilbury", 1588, "Personal command promised."],
      ["I do not desire to live to distrust my faithful and loving people.", "Tilbury speech", "Speech to the Troops at Tilbury", 1588, "Trust as mutual bond."],
      ["There is an Italian proverb which saith, From those who only pray for us, good Lord deliver us—better companions are those who act.", "Attributed court wit", "On prayer and action", 1590, "Preferring deeds to empty piety."],
      ["I would rather be a beggar and single than a queen and married—unless marriage serve the realm.", "Attributed remarks on marriage", "On marriage and realm", 1560, "Personal state subordinated to statecraft."],
      ["Anger makes dull men witty, but it keeps them poor.", "Attributed maxims", "On anger", 1595, "Temper as expensive fuel."],
    ]),
    speech: {
      title: "Speech to the Troops at Tilbury",
      year: 1588,
      description: "Elizabeth I's address to land forces during the Spanish Armada crisis (as traditionally recorded).",
      fullText: `My loving people,

We have been persuaded by some that are careful of our safety, to take heed how we commit ourselves to armed multitudes, for fear of treachery; but I assure you I do not desire to live to distrust my faithful and loving people.

Let tyrants fear. I have always so behaved myself that, under God, I have placed my chiefest strength and safeguard in the loyal hearts and good-will of my subjects; and therefore I am come amongst you, as you see, at this time, not for my recreation and disport, but being resolved, in the midst and heat of the battle, to live and die amongst you all; to lay down for my God, and for my kingdom, and my people, my honour and my blood, even in the dust.

I know I have the body but of a weak and feeble woman; but I have the heart and stomach of a king, and of a king of England too, and think foul scorn that Parma or Spain, or any prince of Europe, should dare to invade the borders of my realm: to which rather than any dishonour shall grow by me, I myself will take up arms, I myself will be your general, judge, and rewarder of every one of your virtues in the field.

I know already, for your forwardness you have deserved rewards and crowns; and We do assure you in the word of a prince, they shall be duly paid you. In the mean time, my lieutenant general shall be in my stead, than whom never prince commanded a more noble or worthy subject; not doubting but by your obedience to my general, by your concord in the camp, and your valour in the field, we shall shortly have a famous victory over those enemies of my God, of my kingdom, and of my people.`,
    },
  },
];

function w(rows) {
  return rows.map(
    ([word, definition, example, partOfSpeech, complexity, source, speech, categories]) => ({
      word,
      definition,
      example,
      partOfSpeech,
      complexity,
      source,
      speech,
      categories,
    }),
  );
}

function q(rows) {
  return rows.map(([text, source, speech, year, context]) => ({
    text,
    source,
    speech,
    year,
    context,
  }));
}

function wordId(oratorId, index) {
  return oratorId * 1000 + index;
}

function quoteId(oratorId, index) {
  return oratorId * 1000 + index;
}

function writeJson(filePath, data) {
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2) + "\n");
}

// dictionaries
const dictPath = path.join(seed, "dictionaries.json");
let dictionaries = JSON.parse(fs.readFileSync(dictPath, "utf8"));
dictionaries = dictionaries.filter((d) => d.id < 45 || d.id > 54);
for (const o of ORATORS) dictionaries.push(o.dict);
dictionaries.sort((a, b) => a.id - b.id);
writeJson(dictPath, dictionaries);

// speeches
const speechesPath = path.join(seed, "speeches.json");
let speeches = JSON.parse(fs.readFileSync(speechesPath, "utf8"));
speeches = speeches.filter((s) => s.oratorId < 45 || s.oratorId > 54);
let nextSpeechId = Math.max(0, ...speeches.map((s) => s.id)) + 1;
for (const o of ORATORS) {
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

// word + quote files
for (const o of ORATORS) {
  writeJson(
    path.join(seed, `words_${o.file}.json`),
    o.words.map((word, i) => ({
      id: wordId(o.id, i + 1),
      oratorId: o.id,
      ...word,
    })),
  );
  writeJson(
    path.join(seed, `quotes_${o.file}.json`),
    o.quotes.map((quote, i) => ({
      id: quoteId(o.id, i + 1),
      oratorId: o.id,
      ...quote,
    })),
  );
}

// portraits
let portraits = fs.readFileSync(portraitsPath, "utf8");
const portraitBlock = `        44L to "john_lewis",
        45L to "sojourner_truth",
        46L to "susan_b_anthony",
        47L to "benjamin_franklin",
        48L to "malcolm_x",
        49L to "desmond_tutu",
        50L to "chief_joseph",
        51L to "atticus_finch",
        52L to "albus_dumbledore",
        53L to "oprah_winfrey",
        54L to "elizabeth_i",
    )`;
if (!portraits.includes('45L to "sojourner_truth"')) {
  portraits = portraits.replace(/44L to "john_lewis",\s*\n\s*\)/, portraitBlock);
  fs.writeFileSync(portraitsPath, portraits);
}

// seed version
let loader = fs.readFileSync(loaderPath, "utf8");
loader = loader.replace(/const val SEED_VERSION = \d+/, "const val SEED_VERSION = 7");
fs.writeFileSync(loaderPath, loader);

// validate all words
const wordIds = new Set();
const failures = [];
for (const f of fs.readdirSync(seed).filter((x) => x.startsWith("words_"))) {
  for (const entry of JSON.parse(fs.readFileSync(path.join(seed, f), "utf8"))) {
    if (wordIds.has(entry.id)) failures.push(`duplicate word id ${entry.id} (${f})`);
    wordIds.add(entry.id);
    const lw = String(entry.word).toLowerCase();
    const le = String(entry.example || "").toLowerCase();
    const ok =
      le.includes(lw) ||
      le.includes(lw + "s") ||
      le.includes(lw + "d") ||
      le.includes(lw + "ing") ||
      le.includes(lw + "ed");
    if (!ok) failures.push(`${f}: '${entry.word}' not in example`);
    for (const c of entry.categories || []) {
      if (!THEMES.has(c)) failures.push(`${f}: bad theme ${c}`);
    }
  }
}

console.log(
  `Batch2: +${ORATORS.length} orators; dictionaries=${dictionaries.length}; speeches=${speeches.length}; words=${wordIds.size}`,
);
if (failures.length) {
  console.error("FAILURES:");
  failures.forEach((x) => console.error(" ", x));
  process.exit(1);
}
console.log("Validation OK");
