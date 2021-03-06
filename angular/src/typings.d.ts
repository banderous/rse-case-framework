import {Party} from './generated/client-lib';

type KeysOfType<T, K> = NonNullable<{
  [P in keyof T]: T[P] extends K ? P : never;
}[keyof T]>;

type PickOfType<T, K> = Pick<T, KeysOfType<T, K>>;

type PartyType = Party['partyType'];
