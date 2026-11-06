const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or create study_sessions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("study_sessions").get());
  await assertFails(unauthDb.collection("study_sessions").doc("s1").get());
});

test("Authenticated user: can create and query own study_sessions, but cannot read another user's session", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("study_sessions").doc("bob_session").set({
      userId: BOB_UID,
      subjectTag: "Engineering",
      durationMinutes: 50,
      distractionsBlocked: 3,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("study_sessions").doc("bob_session").get());

  await assertSucceeds(
    aliceDb.collection("study_sessions").doc("alice_session").set({
      userId: ALICE_UID,
      subjectTag: "Coding",
      durationMinutes: 25,
      distractionsBlocked: 2,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("study_sessions").where("userId", "==", ALICE_UID).get()
  );

  // Fails query without userId filter
  await assertFails(aliceDb.collection("study_sessions").get());
});

test("Shadow Update & Value Poisoning: rejects ghost fields and invalid types on study_sessions", async () => {
  const now = new Date();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("study_sessions").doc("s_valid").set({
      userId: ALICE_UID,
      subjectTag: "Physics",
      durationMinutes: 50,
      distractionsBlocked: 1,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Ghost field attack
  await assertFails(
    aliceDb.collection("study_sessions").doc("s_valid").update({
      isVerified: true,
      updatedAt: now,
    })
  );

  // Value poisoning attack (negative duration)
  await assertFails(
    aliceDb.collection("study_sessions").doc("s_valid").update({
      durationMinutes: -10,
      updatedAt: now,
    })
  );
});

test("Study rooms & room members: enforces host ownership, public visibility filter, and member isolation", async () => {
  const now = new Date();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("study_rooms").doc("room_1").set({
      roomCode: "ENG101",
      roomName: "MIT Linear Algebra Prep",
      subjectTag: "Engineering",
      hostId: ALICE_UID,
      hostName: "Alice",
      visibility: "public",
      timerDurationMinutes: 50,
      activeMembersCount: 1,
      lastReaction: "🔥",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob can query public study rooms with visibility filter
  await assertSucceeds(
    bobDb.collection("study_rooms").where("visibility", "==", "public").get()
  );

  // Bob can send a reaction to public room, but cannot change roomName or delete room
  await assertSucceeds(
    bobDb.collection("study_rooms").doc("room_1").update({
      lastReaction: "🎯",
      activeMembersCount: 2,
      updatedAt: now,
    })
  );
  await assertFails(
    bobDb.collection("study_rooms").doc("room_1").update({
      roomName: "Hacked Room",
      updatedAt: now,
    })
  );
  await assertFails(bobDb.collection("study_rooms").doc("room_1").delete());

  // Bob can create his own room_member entry, but cannot spoof Alice's UID
  await assertFails(
    bobDb.collection("room_members").doc("room_1_alice").set({
      roomId: "room_1",
      userId: ALICE_UID,
      displayName: "Spoofed Alice",
      subjectTag: "Engineering",
      status: "Focusing",
      focusedMinutes: 100,
      distractionCount: 0,
      lastReaction: "🔥",
      visibility: "public",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    bobDb.collection("room_members").doc("room_1_bob").set({
      roomId: "room_1",
      userId: BOB_UID,
      displayName: "Bob",
      subjectTag: "Engineering",
      status: "Focusing",
      focusedMinutes: 25,
      distractionCount: 0,
      lastReaction: "💪",
      visibility: "public",
      createdAt: now,
      updatedAt: now,
    })
  );
});
