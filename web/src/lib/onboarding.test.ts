import { describe, expect, it } from 'vitest';
import { resolveOnboarding } from './onboarding';
import type { VoiceOrator } from './voiceFamily';
import { voiceFamilyOf } from './voiceFamily';

const catalog: VoiceOrator[] = [
  { id: 1, name: 'Cicero', category: 'Ancient Classics', tags: [], themeCategories: [] },
  { id: 2, name: 'Lincoln', category: '19th Century Powerhouses', tags: ['union'], themeCategories: [] },
  { id: 3, name: 'Shakespeare', category: 'Literary Classics', tags: [], themeCategories: [] },
  { id: 4, name: 'Jobs', category: 'Modern / Contemporary', tags: [], themeCategories: ['tech'] },
  { id: 5, name: 'Yoda', category: 'Fictional / Mythic', tags: [], themeCategories: [] },
  { id: 6, name: 'Aurelius', category: 'Historical / Philosophical', tags: [], themeCategories: [] },
];

describe('onboarding answers', () => {
  it('maps voices the way the Android families do', () => {
    expect(voiceFamilyOf(catalog[0]!)).toBe('classical');
    expect(voiceFamilyOf(catalog[1]!)).toBe('statesmen');
    expect(voiceFamilyOf(catalog[2]!)).toBe('literary');
    expect(voiceFamilyOf(catalog[3]!)).toBe('technology');
    expect(voiceFamilyOf(catalog[4]!)).toBe('fictional');
    expect(voiceFamilyOf(catalog[5]!)).toBe('classical');
  });

  it('defaults to classical orators and rotates when more than one is chosen', () => {
    const patch = resolveOnboarding(catalog, new Set(['classical']), new Set(), new Set());
    expect(patch.favoriteOratorIds).toEqual([1, 6]);
    expect(patch.selectedOratorId).toBeNull();
    expect(patch.rotateThroughAll).toBe(true);
    expect(patch.includeLiterary).toBe(false);
    expect(patch.includeFictional).toBe(false);
  });

  it('pins a single orator and follows literary or fictional choices', () => {
    const single = resolveOnboarding(catalog, new Set(['classical']), new Set(), new Set([1]));
    expect(single.selectedOratorId).toBe(1);
    expect(single.rotateThroughAll).toBe(false);
    const invented = resolveOnboarding(catalog, new Set(['literary', 'fictional']), new Set(['arts', 'not-a-theme']), new Set([3, 5]));
    expect(invented.includeLiterary).toBe(true);
    expect(invented.includeFictional).toBe(true);
    expect(invented.selectedThemeCategories).toEqual(['arts']);
    expect(invented.favoriteOratorIds).toEqual([3, 5]);
  });

  it('drops orators outside the chosen families', () => {
    const patch = resolveOnboarding(catalog, new Set(['classical']), new Set(), new Set([1, 4, 5]));
    expect(patch.favoriteOratorIds).toEqual([1]);
    expect(patch.includeFictional).toBe(false);
  });
});
