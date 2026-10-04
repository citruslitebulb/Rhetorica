import { useCallback, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { words } from '../data/catalog';
import { resolveOratorId } from '../data/wordOfDay';
import {
  assembleLetterGuess,
  assembleMultipleChoice,
  guessTarget,
  MAX_UNKNOWN_INPUT_LENGTH,
  MIN_UNKNOWN_GUESS_LENGTH,
  WORD_GUESS_DIFFICULTIES,
  type WordGuessDifficulty,
} from '../lib/quizRound';
import {
  evaluateGuess,
  evaluateGuessAllowingLengthMismatch,
  mergeKeyboardState,
  normalizeGuess,
  type LetterMark,
} from '../lib/wordGuess';
import { useDocumentTitle } from '../lib/useDocumentTitle';
import { useLibrary } from '../state/LibraryProvider';
import type { Word } from '../types';

type QuizMode = 'choice' | 'guess';
type QuizPool = 'library' | 'saved';
type GuessStatus = 'playing' | 'won' | 'lost';

type GuessRow = { letters: string; marks: LetterMark[] };

type QuizState = {
  mode: QuizMode;
  pool: QuizPool;
  difficulty: WordGuessDifficulty;
  unavailable: boolean;
  review: boolean;
  sessionCorrect: number;
  sessionTotal: number;
  prompt: string;
  correct: Word | null;
  options: Word[];
  selectedId: number | null;
  answered: boolean;
  target: string;
  targetDisplay: string;
  revealsLength: boolean;
  currentGuess: string;
  rows: GuessRow[];
  keyboard: ReadonlyMap<string, LetterMark>;
  status: GuessStatus;
  maxAttempts: number;
  message: string | null;
};

const KEY_ROWS = ['qwertyuiop', 'asdfghjkl', 'zxcvbnm'] as const;

function initialState(): QuizState {
  return {
    mode: 'choice',
    pool: 'library',
    difficulty: 'easy',
    unavailable: false,
    review: false,
    sessionCorrect: 0,
    sessionTotal: 0,
    prompt: '',
    correct: null,
    options: [],
    selectedId: null,
    answered: false,
    target: '',
    targetDisplay: '',
    revealsLength: true,
    currentGuess: '',
    rows: [],
    keyboard: new Map(),
    status: 'playing',
    maxAttempts: WORD_GUESS_DIFFICULTIES.easy.maxAttempts,
    message: null,
  };
}

export function QuizPage() {
  useDocumentTitle('Quest');
  const library = useLibrary();
  const libraryRef = useRef(library);
  libraryRef.current = library;
  const [state, setState] = useState<QuizState>(initialState);
  const previousId = useRef<number | null>(null);
  const previousDefinition = useRef('');
  const stateRef = useRef(state);
  stateRef.current = state;

  const begin = useCallback(
    (mode: QuizMode, pool: QuizPool, difficulty: WordGuessDifficulty) => {
      const library = libraryRef.current;
      const visible = new Set(library.visibleOratorIds);
      const oratorId = resolveOratorId(library.preferences.selectedOratorId, library.preferences.rotateThroughAll, visible);
      const libraryIds = new Set(library.libraryOratorIds);
      const savedIds = new Set(library.saved.map((record) => record.wordId));
      const scope = oratorId != null ? new Set([oratorId]) : libraryIds;
      const band = WORD_GUESS_DIFFICULTIES[difficulty];

      if (mode === 'choice') {
        const round = assembleMultipleChoice({
          words,
          savedIds,
          dueWords: library.dueWords(scope, pool === 'saved'),
          pool,
          oratorId,
          libraryOratorIds: libraryIds,
          previousWordId: previousId.current,
          rng: Math.random,
        });
        setState((current) => ({
          ...current,
          mode,
          pool,
          difficulty,
          unavailable: round == null,
          review: round?.review ?? false,
          prompt: round?.correct.definition ?? '',
          correct: round?.correct ?? null,
          options: round?.options ?? [],
          selectedId: null,
          answered: false,
          message: null,
          status: 'playing',
          currentGuess: '',
          rows: [],
          keyboard: new Map(),
        }));
        if (round) {
          previousId.current = round.correct.id;
          previousDefinition.current = round.correct.definition.trim().toLowerCase();
        }
        return;
      }

      const word = assembleLetterGuess({
        words,
        savedIds,
        pool,
        oratorId,
        libraryOratorIds: libraryIds,
        minLetters: band.minLetters,
        maxLetters: band.maxLetters,
        excludeWordIds: new Set(previousId.current == null ? [] : [previousId.current]),
        excludeDefinitions: new Set(previousDefinition.current ? [previousDefinition.current] : []),
        rng: Math.random,
      });
      const target = word ? guessTarget(word) : '';
      setState((current) => ({
        ...current,
        mode,
        pool,
        difficulty,
        unavailable: word == null || target.length === 0,
        review: false,
        prompt: word?.definition ?? '',
        correct: word,
        options: [],
        selectedId: null,
        answered: false,
        target,
        targetDisplay: word?.word ?? '',
        revealsLength: band.revealsLength,
        currentGuess: '',
        rows: [],
        keyboard: new Map(),
        status: 'playing',
        maxAttempts: band.maxAttempts,
        message: null,
      }));
      if (word && target.length > 0) {
        previousId.current = word.id;
        previousDefinition.current = word.definition.trim().toLowerCase();
      }
    },
    [],
  );

  useEffect(() => {
    begin('choice', 'library', 'easy');
  }, [begin]);

  function selectOption(optionId: number) {
    const current = stateRef.current;
    if (current.mode !== 'choice' || current.answered || current.correct == null) return;
    const correct = optionId === current.correct.id;
    library.recordQuizAnswer(current.correct.id, correct);
    setState((snapshot) => ({
      ...snapshot,
      selectedId: optionId,
      answered: true,
      sessionCorrect: snapshot.sessionCorrect + (correct ? 1 : 0),
      sessionTotal: snapshot.sessionTotal + 1,
    }));
  }

  function pressKey(letter: string) {
    setState((current) => {
      if (current.mode !== 'guess' || current.status !== 'playing' || current.unavailable) return current;
      const character = letter.toLowerCase();
      if (!/^[a-z]$/.test(character)) return current;
      const maxLength = current.revealsLength ? current.target.length : MAX_UNKNOWN_INPUT_LENGTH;
      if (current.currentGuess.length >= maxLength) return current;
      return { ...current, currentGuess: current.currentGuess + character, message: null };
    });
  }

  function backspace() {
    setState((current) => {
      if (current.mode !== 'guess' || current.status !== 'playing' || current.currentGuess.length === 0) return current;
      return { ...current, currentGuess: current.currentGuess.slice(0, -1), message: null };
    });
  }

  function submitGuess() {
    const current = stateRef.current;
    if (current.mode !== 'guess' || current.status !== 'playing' || current.target.length === 0) return;
    const guess = normalizeGuess(current.currentGuess);
    if (guess.length === 0) return;
    if (current.revealsLength && guess.length !== current.target.length) {
      setState((snapshot) => ({ ...snapshot, message: `Use exactly ${snapshot.target.length} letters.` }));
      return;
    }
    if (!current.revealsLength && guess.length < MIN_UNKNOWN_GUESS_LENGTH) {
      setState((snapshot) => ({ ...snapshot, message: `Enter at least ${MIN_UNKNOWN_GUESS_LENGTH} letters.` }));
      return;
    }
    const marks = current.revealsLength
      ? evaluateGuess(guess, current.target)
      : evaluateGuessAllowingLengthMismatch(guess, current.target);
    const rows = [...current.rows, { letters: guess, marks }];
    const won = guess === current.target;
    const lost = !won && rows.length >= current.maxAttempts;
    if ((won || lost) && current.correct) library.recordQuizAnswer(current.correct.id, won);
    setState((snapshot) => ({
      ...snapshot,
      rows,
      currentGuess: '',
      keyboard: mergeKeyboardState(snapshot.keyboard, guess, marks),
      status: won ? 'won' : lost ? 'lost' : 'playing',
      sessionCorrect: snapshot.sessionCorrect + (won ? 1 : 0),
      sessionTotal: snapshot.sessionTotal + (won || lost ? 1 : 0),
      message: null,
    }));
  }

  useEffect(() => {
    function onKey(event: KeyboardEvent) {
      const target = event.target;
      if (target instanceof HTMLInputElement || target instanceof HTMLTextAreaElement || target instanceof HTMLSelectElement) {
        return;
      }
      if (stateRef.current.mode !== 'guess') return;
      if (event.key === 'Enter') {
        event.preventDefault();
        submitGuess();
      } else if (event.key === 'Backspace') {
        event.preventDefault();
        backspace();
      } else if (/^[a-zA-Z]$/.test(event.key)) {
        event.preventDefault();
        pressKey(event.key);
      }
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, []);

  const unavailableBody =
    state.pool === 'saved'
      ? 'Save at least four words to quiz yourself from your collection.'
      : state.mode === 'guess'
        ? 'No matching words for this difficulty. Try another setting.'
        : 'Need at least a few words in the library before starting a quest.';

  return (
    <div className="page">
      <header className="page-header">
        <p className="kicker">Spaced repetition</p>
        <h1>Quest</h1>
        <p className="lede">Match a definition, or guess the word letter by letter. Due words come back first.</p>
      </header>

      <div className="chip-row" role="group" aria-label="Quest mode">
        <button type="button" className={state.mode === 'choice' ? 'chip is-on' : 'chip'} aria-pressed={state.mode === 'choice'} onClick={() => { if (state.mode !== 'choice') begin('choice', state.pool, state.difficulty); }}>
          Multiple choice
        </button>
        <button type="button" className={state.mode === 'guess' ? 'chip is-on' : 'chip'} aria-pressed={state.mode === 'guess'} onClick={() => { if (state.mode !== 'guess') begin('guess', state.pool, state.difficulty); }}>
          Letter guess
        </button>
      </div>
      <div className="chip-row" role="group" aria-label="Word pool">
        <button type="button" className={state.pool === 'library' ? 'chip is-on' : 'chip'} aria-pressed={state.pool === 'library'} onClick={() => { if (state.pool !== 'library') begin(state.mode, 'library', state.difficulty); }}>
          Library
        </button>
        <button type="button" className={state.pool === 'saved' ? 'chip is-on' : 'chip'} aria-pressed={state.pool === 'saved'} onClick={() => { if (state.pool !== 'saved') begin(state.mode, 'saved', state.difficulty); }}>
          Saved words
        </button>
      </div>

      {state.sessionTotal > 0 ? (
        <p className="kicker">
          Session score: {state.sessionCorrect} / {state.sessionTotal}
        </p>
      ) : null}

      {state.unavailable ? (
        <div className="empty">
          <p className="empty-title">Quest not ready</p>
          <p>{unavailableBody}</p>
          <button type="button" className="button" onClick={() => begin(state.mode, state.pool, state.difficulty)}>
            Try again
          </button>
          {state.pool === 'saved' ? (
            <Link className="button" to="/words">
              Browse words
            </Link>
          ) : null}
        </div>
      ) : state.mode === 'choice' ? (
        <ChoiceRound state={state} onSelect={selectOption} onNext={() => begin('choice', state.pool, state.difficulty)} />
      ) : (
        <GuessRound
          state={state}
          onDifficulty={(difficulty) => begin('guess', state.pool, difficulty)}
          onKey={pressKey}
          onBackspace={backspace}
          onSubmit={submitGuess}
          onNext={() => begin('guess', state.pool, state.difficulty)}
        />
      )}
    </div>
  );
}

function ChoiceRound({
  state,
  onSelect,
  onNext,
}: {
  state: QuizState;
  onSelect: (id: number) => void;
  onNext: () => void;
}) {
  return (
    <section className="quest-round">
      <p className="meta">Match the definition to the correct word.</p>
      {state.review ? <p className="review-badge">Review · you have seen this word before</p> : null}
      <h2>Definition</h2>
      <p className="prose">{state.prompt}</p>
      <ul className="option-list">
        {state.options.map((option) => {
          const chosen = state.selectedId === option.id;
          const correct = state.answered && state.correct?.id === option.id;
          const wrong = state.answered && chosen && !correct;
          const className = ['option', correct ? 'is-correct' : '', wrong ? 'is-wrong' : ''].filter(Boolean).join(' ');
          return (
            <li key={option.id}>
              <button type="button" className={className} disabled={state.answered} onClick={() => onSelect(option.id)}>
                <span>{option.word}</span>
                <span className="meta">{option.partOfSpeech}</span>
              </button>
            </li>
          );
        })}
      </ul>
      {state.answered ? (
        <div className="quest-feedback">
          <p>{state.selectedId === state.correct?.id ? 'Well chosen — that matches the definition.' : 'Not quite. The correct word is highlighted.'}</p>
          {state.correct ? (
            <Link className="text-link" to={`/words/${state.correct.id}`}>
              Open {state.correct.word}
            </Link>
          ) : null}
          <button type="button" className="button primary" onClick={onNext}>
            Next question
          </button>
        </div>
      ) : null}
    </section>
  );
}

function GuessRound({
  state,
  onDifficulty,
  onKey,
  onBackspace,
  onSubmit,
  onNext,
}: {
  state: QuizState;
  onDifficulty: (difficulty: WordGuessDifficulty) => void;
  onKey: (letter: string) => void;
  onBackspace: () => void;
  onSubmit: () => void;
  onNext: () => void;
}) {
  const blanks = state.revealsLength ? state.target.length : Math.max(state.currentGuess.length, 1);
  return (
    <section className="quest-round">
      <div className="chip-row" role="group" aria-label="Difficulty">
        {(Object.keys(WORD_GUESS_DIFFICULTIES) as WordGuessDifficulty[]).map((difficulty) => (
          <button
            key={difficulty}
            type="button"
            className={state.difficulty === difficulty ? 'chip is-on' : 'chip'}
            aria-pressed={state.difficulty === difficulty}
            onClick={() => { if (state.difficulty !== difficulty) onDifficulty(difficulty); }}
          >
            {difficulty === 'hardcore' ? 'Hardcore' : difficulty.charAt(0).toUpperCase() + difficulty.slice(1)}
          </button>
        ))}
      </div>
      <p className="meta">Read the definition, then guess the word. You have {state.maxAttempts} attempts.</p>
      <h2>Definition</h2>
      <p className="prose">{state.prompt}</p>
      <div className="guess-board" aria-label="Guesses">
        {state.rows.map((row, rowIndex) => (
          <div key={`${row.letters}-${rowIndex}`} className="guess-row">
            {row.marks.map((mark, index) => (
              <span key={`${row.letters}-${index}`} className={`tile is-${mark}`}>
                {row.letters[index] ?? ''}
              </span>
            ))}
          </div>
        ))}
        {state.status === 'playing' ? (
          <div className="guess-row" aria-label="Current guess">
            {Array.from({ length: blanks }, (_, index) => (
              <span key={index} className="tile">
                {state.currentGuess[index] ?? ''}
              </span>
            ))}
          </div>
        ) : null}
      </div>
      {state.message ? <p className="meta">{state.message}</p> : null}
      {state.status === 'won' ? <p className="quest-feedback">Correct — {state.targetDisplay}</p> : null}
      {state.status === 'lost' ? <p className="quest-feedback">Out of attempts. The word was {state.targetDisplay}.</p> : null}
      {state.status === 'playing' ? (
        <div className="keyboard" aria-label="Letters">
          {KEY_ROWS.map((row) => (
            <div key={row} className="keyboard-row">
              {row === 'zxcvbnm' ? (
                <button type="button" className="key key-wide" onClick={onSubmit}>
                  Enter
                </button>
              ) : null}
              {[...row].map((letter) => (
                <button key={letter} type="button" className={`key is-${state.keyboard.get(letter) ?? 'unused'}`} onClick={() => onKey(letter)}>
                  {letter}
                </button>
              ))}
              {row === 'zxcvbnm' ? (
                <button type="button" className="key key-wide" onClick={onBackspace} aria-label="Delete">
                  ⌫
                </button>
              ) : null}
            </div>
          ))}
        </div>
      ) : (
        <div className="quest-feedback">
          {state.correct ? (
            <Link className="text-link" to={`/words/${state.correct.id}`}>
              Open {state.correct.word}
            </Link>
          ) : null}
          <button type="button" className="button primary" onClick={onNext}>
            Next word
          </button>
        </div>
      )}
    </section>
  );
}
